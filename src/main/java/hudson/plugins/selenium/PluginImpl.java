/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package hudson.plugins.selenium;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher.LocalLauncher;
import hudson.Plugin;
import hudson.Proc;
import hudson.console.HyperlinkNote;
import hudson.model.Action;
import hudson.model.Describable;
import hudson.model.Failure;
import hudson.model.TaskListener;
import hudson.model.Api;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Label;
import hudson.plugins.selenium.callables.SeleniumCallable;
import hudson.plugins.selenium.callables.SeleniumConstants;
import hudson.plugins.selenium.callables.running.RemoteGetConfigurations;
import hudson.plugins.selenium.callables.running.RemoteGetStatus;
import hudson.plugins.selenium.configuration.ConfigurationDescriptor;
import hudson.plugins.selenium.configuration.CustomRCConfiguration;
import hudson.plugins.selenium.configuration.CustomWDConfiguration;
import hudson.plugins.selenium.configuration.SeleniumNodeConfiguration;
import hudson.plugins.selenium.configuration.browser.selenium.ChromeBrowser;
import hudson.plugins.selenium.configuration.browser.selenium.FirefoxBrowser;
import hudson.plugins.selenium.configuration.browser.selenium.IEBrowser;
import hudson.plugins.selenium.configuration.browser.selenium.SeleniumBrowser;
import hudson.plugins.selenium.configuration.browser.webdriver.WebDriverBrowser;
import hudson.plugins.selenium.configuration.global.SeleniumGlobalConfiguration;
import hudson.plugins.selenium.configuration.global.matcher.MatchAllMatcher;
import hudson.plugins.selenium.configuration.global.matcher.SeleniumConfigurationMatcher;
import hudson.plugins.selenium.configuration.global.matcher.SeleniumConfigurationMatcher.MatcherDescriptor;
import hudson.remoting.Callable;
import hudson.remoting.Channel;
import hudson.remoting.Launcher;
import hudson.remoting.SocketInputStream;
import hudson.remoting.SocketOutputStream;
import hudson.remoting.VirtualChannel;
import hudson.remoting.Which;
import hudson.security.Permission;
import hudson.slaves.Channels;
import hudson.util.ClasspathBuilder;
import hudson.util.IOException2;
import hudson.util.JVMBuilder;
import hudson.util.StreamTaskListener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.framework.io.LargeText;
import org.openqa.grid.internal.Registry;
import org.openqa.grid.internal.RemoteProxy;
import org.openqa.grid.internal.TestSlot;
import org.openqa.grid.selenium.GridLauncher;
import org.springframework.util.StringUtils;

/**
 * Starts Selenium Grid server in another JVM.
 * 
 * @author Kohsuke Kawaguchi
 * @author Richard Lavoie
 */
@ExportedBean
public class PluginImpl extends Plugin implements Action, Serializable,
		Describable<PluginImpl> {

	private static final String SEPARATOR = ",";

	private static final Logger LOGGER = Logger.getLogger(PluginImpl.class
			.getName());

	private int port = 4444;
	private String exclusionPatterns;
	private Integer newSessionWaitTimeout = -1;
	private boolean throwOnCapabilityNotPresent = false;
	private String hubLogLevel = "INFO";
	private boolean rcDebug;
	private String rcLog;

	private List<SeleniumGlobalConfiguration> configurations = new ArrayList<SeleniumGlobalConfiguration>();

	/**
	 * Channel to Selenium Grid JVM.
	 */
	private transient Channel channel;

	private transient Future<?> hubLauncher;

	private transient static Set<Computer> launchedComputers = new HashSet<Computer>();

	@Override
	public void postInitialize() throws Exception {
		load();

		StreamTaskListener listener = new StreamTaskListener(getLogFile());
		File root = Hudson.getInstance().getRootDir();
		channel = createSeleniumGridVM(root, listener);
		Level logLevel = Level.parse(getHubLogLevel());
		System.out.println("Starting Selenium Grid");

		List<String> args = new ArrayList<String>();
		if (getNewSessionWaitTimeout() != null
				&& getNewSessionWaitTimeout() >= 0) {
			args.add("-newSessionWaitTimeout");
			args.add(getNewSessionWaitTimeout().toString());
		}
		if (getThrowOnCapabilityNotPresent()) {
			args.add("-throwOnCapabilityNotPresent");
			args.add(Boolean.toString(getThrowOnCapabilityNotPresent()));
		}

		args.add("-host");
		args.add(getMasterHostName());
		
		hubLauncher = channel.callAsync(new HubLauncher(port, args
				.toArray(new String[0]), logLevel));

		Hudson.getInstance().getActions().add(this);
	}

	public File getLogFile() {
		return new File(Hudson.getInstance().getRootDir(), "selenium.log");
	}

	@Override
	public void configure(StaplerRequest req, JSONObject formData) {
		port = formData.getInt("port");
		exclusionPatterns = formData.getString("exclusionPatterns");
		rcLog = formData.getString("rcLog");
		rcDebug = formData.getBoolean("rcDebug");
		newSessionWaitTimeout = formData.getInt("newSessionWaitTimeout");
		throwOnCapabilityNotPresent = formData
				.getBoolean("throwOnCapabilityNotPresent");
		hubLogLevel = formData.getString("hubLogLevel");
		// rcBrowserSideLog = formData.getBoolean("rcBrowserSideLog");
		// rcTrustAllSSLCerts = formData.getBoolean("rcTrustAllSSLCerts");
		// rcBrowserSessionReuse = formData.getBoolean("rcBrowserSessionReuse");
		// rcFirefoxProfileTemplate =
		// formData.getString("rcFirefoxProfileTemplate");
		try {
			save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void waitForHubLaunch() throws ExecutionException,
			InterruptedException {
		hubLauncher.get();
	}

	public String getIconFileName() {
		return "/plugin/selenium/24x24/selenium.png";
	}

	public String getDisplayName() {
		return "Selenium Grid";
	}

	public String getUrlName() {
		return "/selenium";
	}

	public Api getApi() {
		return new Api(this);
	}

	@Exported
	public int getPort() {
		return port;
	}

	@Exported
	public String getExclusionPatterns() {
		return exclusionPatterns;
	}

	@Exported
	public Integer getNewSessionWaitTimeout() {
		return newSessionWaitTimeout;
	}

	@Exported
	public String getHubLogLevel() {
		return hubLogLevel != null ? hubLogLevel : "INFO";
	}

	@Exported
	public boolean getRcDebug() {
		return rcDebug;
	}

	@Exported
	public String getRcLog() {
		return rcLog;
	}

	@Exported
	public boolean getThrowOnCapabilityNotPresent() {
		return throwOnCapabilityNotPresent;
	}

	@Override
	public void stop() throws Exception {
		channel.close();
	}

	@Exported(inline = true)
	public List<SeleniumTestSlot> getRemoteControls() throws IOException,
			InterruptedException {
		if (channel == null)
			return Collections.emptyList();

		return channel
				.call(new Callable<List<SeleniumTestSlot>, RuntimeException>() {
					/**
			 * 
			 */
					private static final long serialVersionUID = 1791985298575049757L;

					public List<SeleniumTestSlot> call()
							throws RuntimeException {
						List<SeleniumTestSlot> r = new ArrayList<SeleniumTestSlot>();

						Registry registry = RegistryHolder.registry;
						if (registry != null) {
							for (RemoteProxy proxy : registry.getAllProxies()) {
								for (TestSlot slot : proxy.getTestSlots())
									r.add(new SeleniumTestSlot(slot));
							}
						}

						return r;
					}
				});
	}

	/**
	 * Launches Hub in a separate JVM.
	 * 
	 * @param rootDir
	 *            The slave/master root.
	 */
	static/* package */Channel createSeleniumGridVM(File rootDir,
			TaskListener listener) throws IOException, InterruptedException {

		JVMBuilder vmb = new JVMBuilder();
		// vmb.debug(8000);
		vmb.systemProperties(null);
		return Channels
				.newJVM("Selenium Grid", listener, vmb, new FilePath(rootDir),
						new ClasspathBuilder().add(findStandAloneServerJar()));
	}

	/**
	 * Locate the stand-alone server jar from the classpath. Only works on the
	 * master.
	 */
	/* package */static File findStandAloneServerJar() throws IOException {
		return Which.jarFile(GridLauncher.class);
	}

	/**
	 * Launches RC in a separate JVM.
	 * 
	 * @param standaloneServerJar
	 *            The jar file of the grid to launch.
	 */
	static public Channel createSeleniumRCVM(File standaloneServerJar,
			TaskListener listener, Map<String, String> properties,
			Map<String, String> envVariables) throws IOException,
			InterruptedException {
		/*
		 * return Channels.newJVM("Selenium RC",listener,null, new
		 * ClasspathBuilder().add(standaloneServerJar), properties);
		 */
		String displayName = "Selenium RC";

		ClasspathBuilder classpath = new ClasspathBuilder()
				.add(standaloneServerJar);
		JVMBuilder vmb = new JVMBuilder();
		vmb.systemProperties(properties);

		ServerSocket serverSocket = new ServerSocket();
		serverSocket.bind(new InetSocketAddress("localhost", 0));
		serverSocket.setSoTimeout(10000);

		// use -cp + FQCN instead of -jar since remoting.jar can be rebundled
		// (like in the case of the swarm plugin.)
		vmb.classpath().addJarOf(Channel.class);
		vmb.mainClass(Launcher.class);

		if (classpath != null)
			vmb.args().add("-cp").add(classpath);
		vmb.args()
				.add("-connectTo", "localhost:" + serverSocket.getLocalPort());

		listener.getLogger().println("Starting " + displayName);

		// TODO add XVFB options here
		Proc p = vmb.launch(new LocalLauncher(listener)).stdout(listener)
				.envs(envVariables).start();

		Socket s = serverSocket.accept();
		serverSocket.close();

		return Channels.forProcess("Channel to " + displayName,
				Computer.threadPoolForRemoting, new BufferedInputStream(
						new SocketInputStream(s)), new BufferedOutputStream(
						new SocketOutputStream(s)), null, p);
	}

	/**
	 * Determines the host name of the Jenkins master.
	 */
	public static String getMasterHostName() throws MalformedURLException {
		String rootUrl = Hudson.getInstance().getRootUrl();
		if (rootUrl == null)
			return "localhost";
		URL url = new URL(rootUrl);
		return url.getHost();
	}

	/**
	 * Handles incremental log.
	 */
	public void doProgressiveLog(StaplerRequest req, StaplerResponse rsp)
			throws IOException {
		new LargeText(getLogFile(), false).doProgressText(req, rsp);
	}

	@SuppressWarnings("unchecked")
	public Descriptor<PluginImpl> getDescriptor() {
		return Hudson.getInstance().getDescriptorOrDie(getClass());
	}

	@Extension
	public static class DescriptorImpl extends Descriptor<PluginImpl> {
		@Override
		public String getDisplayName() {
			return "";
		}
	}

	private static final long serialVersionUID = 1L;

	// this part take cares of the migration from 2.0 to 2.1
	public Object readResolve() {

		if (rcFirefoxProfileTemplate != null || rcBrowserSessionReuse != null
				|| rcTrustAllSSLCerts != null || rcBrowserSideLog != null) {
			String rcFirefoxProfileTemplate_ = getDefaultForNull(
					rcFirefoxProfileTemplate, "");
			Boolean rcBrowserSessionReuse_ = getDefaultForNull(
					rcBrowserSessionReuse, Boolean.FALSE);
			Boolean rcTrustAllSSLCerts_ = getDefaultForNull(rcTrustAllSSLCerts,
					Boolean.FALSE);
			Boolean rcBrowserSideLog_ = getDefaultForNull(rcBrowserSideLog,
					Boolean.FALSE);

			List<SeleniumBrowser> browsers = new ArrayList<SeleniumBrowser>();
			browsers.add(new IEBrowser(5, "", ""));
			browsers.add(new FirefoxBrowser(5, "", ""));
			browsers.add(new ChromeBrowser(5, "", ""));

			int port_ = 4445;
			try {
				ServerSocket ss = new ServerSocket(0);
				port_ = ss.getLocalPort();
				ss.close();
			} catch (IOException e) {
			}

			SeleniumNodeConfiguration c = new CustomRCConfiguration(port_,
					rcBrowserSideLog_, rcDebug, rcTrustAllSSLCerts_,
					rcBrowserSessionReuse_, -1, rcFirefoxProfileTemplate_,
					browsers, null);

            synchronized(configurations) {
                configurations.add(new SeleniumGlobalConfiguration(
                        "Selenium v2.0 configuration", new MatchAllMatcher(), c));
            }

		}

		return this;
	}

	private <T> T getDefaultForNull(T object, T defObject) {
		return object == null ? defObject : object;
	}

	// Kept only for backward compatibility...
	private transient String rcFirefoxProfileTemplate;
	private transient Boolean rcBrowserSessionReuse;
	private transient Boolean rcTrustAllSSLCerts;
	private transient Boolean rcBrowserSideLog;

	public static void startSeleniumNode(Computer c, TaskListener listener, String conf)
			throws IOException, InterruptedException {
		LOGGER.fine("Examining if we need to start Selenium Grid Node");

		final PluginImpl p = Hudson.getInstance().getPlugin(PluginImpl.class);

		final String exclusions = p.getExclusionPatterns();
		List<String> exclusionPatterns = new ArrayList<String>();
		if (StringUtils.hasText(exclusions)) {
			exclusionPatterns = Arrays.asList(exclusions.split(SEPARATOR));
		}
		if (exclusionPatterns.size() > 0) {
			// loop over all the labels and check if we need to exclude a node
			// based on the exlusionPatterns
			for (Label l : c.getNode().getAssignedLabels()) {
				for (String pattern : exclusionPatterns) {
					if (l.toString().matches(pattern)) {
						LOGGER.fine("Node "
								+ c.getNode().getDisplayName()
								+ " is excluded from Selenium Grid because its label '"
								+ l + "' matches exclusion pattern '" + pattern
								+ "'");
						return;
					}
				}
			}
		}

		final String masterName = PluginImpl.getMasterHostName();
		if (masterName == null) {
			listener.getLogger()
					.println(
							"Unable to determine the host name of the master. Skipping Selenium execution. "
									+ "Please "
									+ HyperlinkNote.encodeTo("/configure",
											"configure the Jenkins URL")
									+ " from the system configuration screen.");
			return;
		}
//		final String hostName = c.getHostName();
//		if (hostName == null) {
//			listener.getLogger()
//					.println(
//							"Unable to determine the host name. Skipping Selenium execution.");
//			return;
//		}
		// final int masterPort = p.getPort();
//		final StringBuilder labelList = new StringBuilder();
//		for (Label l : c.getNode().getAssignedLabels()) {
//			labelList.append('/');
//			labelList.append(l);
//		}
//		labelList.append('/');

		// make sure that Selenium Hub is started before we start RCs.
		try {
			p.waitForHubLaunch();
		} catch (ExecutionException e) {
			throw new IOException2(
					"Failed to wait for the Hub launch to complete", e);
		}

		List<SeleniumGlobalConfiguration> confs = getPlugin().getGlobalConfigurationForComputer(c);
		if (confs == null || confs.size() == 0) {
			LOGGER.fine("There is no matching configurations for that computer. Skipping selenium execution.");
			return;
		}
		
		String nodehost = c.getHostName();
		if (nodehost == null) {
			LOGGER.warning("Unable to determine node's hostname. Skipping");
			return;
		}

		listener.getLogger().println("Starting Selenium Grid nodes on " + c.getName());

		final FilePath seleniumJar = new FilePath(
				PluginImpl.findStandAloneServerJar());
		final String nodeName = c.getName();

		for (SeleniumGlobalConfiguration config : confs) {
			try {
				listener.getLogger().println("Testing " + config);
				if ((conf != null && config.getName().equals(conf)) || conf == null) {
					listener.getLogger().println("Starting " + config);
                    SeleniumRunOptions opts = config.getOptions(c);
                    if (opts == null) {
                        LOGGER.fine("The specified configuration returned null, skipping.");
                    } else {
                        Future<Object> future = c
                                .getNode()
                                .getRootPath()
                                .actAsync(
                                        new SeleniumCallable(seleniumJar, nodehost,
                                                masterName, p.getPort(), nodeName,
                                                listener, config.getName(), opts));
                        future.get();
                        launchedComputers.add(c);
                    }
				}
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	
	public static PluginImpl getPlugin() {
		return Jenkins.getInstance().getPlugin(PluginImpl.class);
	}

	@Exported
	public List<SeleniumGlobalConfiguration> getGlobalConfigurations() {
		return configurations;
	}

	/**
	 * Returns the computer channel only if selenium has been started on this
	 * node
	 * 
	 * @param computer
	 * @return
	 */
	public static VirtualChannel getChannel(Computer computer) {
		if (launchedComputers.contains(computer)) {
			return computer.getNode().getChannel();
		}

		return null;
	}

	public boolean hasGlobalConfiguration(String name) {
		
		return getGlobalConfigurationWithName(name) != null;
	}

	public void removeGlobalConfigurations(String name) {
		Iterator<SeleniumGlobalConfiguration> it = configurations.iterator();
		while (it.hasNext()) {
			SeleniumGlobalConfiguration conf = it.next();
			if (conf.getName().equals(name)) {
				it.remove();

				// there should only be one config with that name
				return;
			}
		}
	}

	public List<SeleniumGlobalConfiguration> getGlobalConfigurationForComputer(
			Computer computer) {
		List<SeleniumGlobalConfiguration> confs = new ArrayList<SeleniumGlobalConfiguration>();
		for (SeleniumGlobalConfiguration c : PluginImpl.getPlugin()
				.getGlobalConfigurations()) {
			if (c.getMatcher().match(computer.getNode())) {
				confs.add(c);
			}
		}
		return confs;

	}

	public SeleniumGlobalConfiguration getGlobalConfigurationWithName(
			String name) {
		for (SeleniumGlobalConfiguration c : configurations) {
			if (name.equals(c.getName())) {
				return c;
			}
		}
		return null;
	}
	
	public SeleniumGlobalConfiguration getConfiguration(String name) {
		return getGlobalConfigurationWithName(name);
	}
	
    /**
     * 
     * @param req StaplerRequest
     * @param rsp StaplerResponse to redirect with
     * @throws IOException if redirection goes wrong
     */
    public void doAddRedirect(StaplerRequest req, StaplerResponse rsp) throws IOException {
        Hudson.getInstance().checkPermission(getRequiredPermission());
        rsp.sendRedirect2("add");
    }
    
    /**
     * 
     * @param req StaplerRequest
     * @param rsp StaplerResponse to redirect with
     * @throws IOException if redirection goes wrong
     */
    public void doCreate(StaplerRequest req, StaplerResponse rsp) throws Exception {
        Hudson.getInstance().checkPermission(getRequiredPermission());
        SeleniumGlobalConfiguration conf = req.bindJSON(SeleniumGlobalConfiguration.class, req.getSubmittedForm());
        if (null == conf.getName() || conf.getName().trim().equals("")) {
        	throw new Failure("You must specify a name for the configuration");
        } 
        
        if (PluginImpl.getPlugin().hasGlobalConfiguration(conf.getName())) {
        	throw new Failure("The configuration name you have chosen is already taken, please choose a unique name.");
        }
        
        PluginImpl.getPlugin().getGlobalConfigurations().add(conf);
        PluginImpl.getPlugin().save();
        rsp.sendRedirect2("configurations");
    }
    
	public Permission getRequiredPermission() {
		return Hudson.ADMINISTER;
	}
 
	public DescriptorExtensionList<SeleniumNodeConfiguration, ConfigurationDescriptor> getConfigTypes() {
		return SeleniumNodeConfiguration.all();
	}

	public DescriptorExtensionList<SeleniumConfigurationMatcher, MatcherDescriptor> getMatcherTypes() {
		return SeleniumConfigurationMatcher.all();
	}
	
	public Map<Computer, List<SeleniumGlobalConfiguration>> getComputers() {
		Map<Computer, List<SeleniumGlobalConfiguration>> cps = new TreeMap<Computer, List<SeleniumGlobalConfiguration>>(new Comparator<Computer>() {

			public int compare(Computer o1, Computer o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		for (Computer c : Jenkins.getInstance().getComputers()) {
			List<SeleniumGlobalConfiguration> confs = getGlobalConfigurationForComputer(c);
			if (confs != null && confs.size() > 0) {
				cps.put(c, confs);
			}
		}
		return cps;
		
	}

	public static Set<String> getRunningConfigurations(Computer computer) {
		try {
			VirtualChannel channel = PluginImpl.getChannel(computer);
			if (channel == null) return Collections.emptySet();
			return channel.call(new RemoteGetConfigurations());
		} catch (Throwable e) {
			return null;
		}
	}
	
	public static String getStatus(Computer computer, String conf) {
		try {
			VirtualChannel channel = PluginImpl.getChannel(computer);
			if (channel == null) return SeleniumConstants.STOPPED;
			return channel.call(new RemoteGetStatus(conf));
		} catch (Throwable e) {
			return "An error occured while retrieving the status of the selenium process " + e.getMessage();
		}
	}
	
	public Channel getHubChannel () {
		return channel;
	}


    public SeleniumConfigurationMatcher getDefaultMatcher() {
        return new MatchAllMatcher();
    }

    public SeleniumNodeConfiguration getDefaultConfiguration() {
    	List<WebDriverBrowser> bs = new ArrayList<WebDriverBrowser>();
    	bs.add(new hudson.plugins.selenium.configuration.browser.webdriver.IEBrowser(1, null, null));
    	bs.add(new hudson.plugins.selenium.configuration.browser.webdriver.FirefoxBrowser(5, null, null));
    	bs.add(new hudson.plugins.selenium.configuration.browser.webdriver.ChromeBrowser(5, null, null));
        return new CustomWDConfiguration(4445, null, bs, null);
    }
}