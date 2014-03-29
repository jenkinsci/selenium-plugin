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
import hudson.Plugin;
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
import hudson.plugins.selenium.configuration.ConfigurationDescriptor;
import hudson.plugins.selenium.configuration.SeleniumNodeConfiguration;
import hudson.plugins.selenium.configuration.CustomRCConfiguration;
import hudson.plugins.selenium.configuration.CustomWDConfiguration;
import hudson.plugins.selenium.configuration.browser.selenium.SeleniumBrowser;
import hudson.plugins.selenium.configuration.browser.selenium.ChromeBrowser;
import hudson.plugins.selenium.configuration.browser.selenium.FirefoxBrowser;
import hudson.plugins.selenium.configuration.browser.selenium.IEBrowser;
import hudson.plugins.selenium.configuration.browser.webdriver.WebDriverBrowser;
import hudson.plugins.selenium.configuration.global.SeleniumGlobalConfiguration;
import hudson.plugins.selenium.configuration.global.hostname.HostnameResolver;
import hudson.plugins.selenium.configuration.global.hostname.HostnameResolverDescriptor;
import hudson.plugins.selenium.configuration.global.hostname.JenkinsRootHostnameResolver;
import hudson.plugins.selenium.configuration.global.matcher.SeleniumConfigurationMatcher;
import hudson.plugins.selenium.configuration.global.matcher.SeleniumConfigurationMatcher.MatcherDescriptor;
import hudson.plugins.selenium.configuration.global.matcher.MatchAllMatcher;
import hudson.plugins.selenium.process.SeleniumProcessUtils;
import hudson.remoting.Callable;
import hudson.remoting.Channel;
import hudson.security.Permission;
import hudson.util.IOException2;
import hudson.util.StreamTaskListener;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import jenkins.model.Jenkins;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.framework.io.LargeText;
import org.openqa.grid.internal.Registry;
import org.openqa.grid.internal.RemoteProxy;
import org.openqa.grid.internal.TestSlot;
import org.springframework.util.StringUtils;

/**
 * Starts Selenium Grid server in another JVM.
 * 
 * @author Kohsuke Kawaguchi
 * @author Richard Lavoie
 */
@ExportedBean
public class PluginImpl extends Plugin implements Action, Serializable, Describable<PluginImpl> {

    private static final String SEPARATOR = ",";

    private static final Logger LOGGER = Logger.getLogger(PluginImpl.class.getName());

    /**
     * Default port for hub servlet.
     */
    private int port = 4444;

    /**
     * Exclusion pattern for nodes. Nodes matching this pattern will not have a selenium node running on them.
     */
    private String exclusionPatterns;
    private Integer newSessionWaitTimeout = -1;
    private boolean throwOnCapabilityNotPresent = false;
    private String hubLogLevel = "INFO";
    private boolean rcDebug;
    private String rcLog;

    private HostnameResolver hostnameResolver;

    // Kept only for backward compatibility...
    private transient String rcFirefoxProfileTemplate;
    private transient Boolean rcBrowserSessionReuse;
    private transient Boolean rcTrustAllSSLCerts;
    private transient Boolean rcBrowserSideLog;

    private List<SeleniumGlobalConfiguration> configurations = new ArrayList<SeleniumGlobalConfiguration>();

    /**
     * Channel to Selenium Grid JVM.
     */
    private transient Channel channel;

    private transient Future<?> hubLauncher;

    private transient StreamTaskListener listener;

    @Override
    public void postInitialize() throws Exception {
        load();

        this.listener = new StreamTaskListener(getLogFile());

        channel = SeleniumProcessUtils.createSeleniumGridVM(listener);

        Level logLevel = Level.parse(getHubLogLevel());
        System.out.println("Starting Selenium Grid");

        List<String> args = new ArrayList<String>();
        if (getNewSessionWaitTimeout() != null && getNewSessionWaitTimeout() >= 0) {
            args.add("-newSessionWaitTimeout");
            args.add(getNewSessionWaitTimeout().toString());
        }
        if (getThrowOnCapabilityNotPresent()) {
            args.add("-throwOnCapabilityNotPresent");
            args.add(Boolean.toString(getThrowOnCapabilityNotPresent()));
        }

        args.add("-host");
        args.add(getMasterHostName());

        hubLauncher = channel.callAsync(new HubLauncher(port, args.toArray(new String[0]), logLevel));

        Hudson.getInstance().getActions().add(this);
    }

    public File getLogFile() {
        return new File(Hudson.getInstance().getRootDir(), "selenium.log");
    }

    public void waitForHubLaunch() throws ExecutionException, InterruptedException {
        hubLauncher.get();
    }

    public String getDisplayName() {
        return "Selenium Grid";
    }

    public String getIconFileName() {
        return Jenkins.getInstance().hasPermission(getRequiredPermission()) ? "/plugin/selenium/24x24/selenium.png" : null;
    }

    public String getUrlName() {
        return "/selenium";
    }

    @Exported
    public int getPort() {
        return port;
    }

    @Exported
    public HostnameResolver getHostnameResolver() {
        return hostnameResolver;
    }

    public Api getApi() {
        return Jenkins.getInstance().hasPermission(getRequiredPermission()) ? new Api(this) : null;
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
        for (Computer c : Jenkins.getInstance().getComputers()) {
            for (SeleniumGlobalConfiguration cfg : configurations) {
                cfg.stop(c);
            }
        }

        this.listener.closeQuietly();
        channel.close();

    }

    @Exported( inline = true )
    public Collection<SeleniumTestSlotGroup> getRemoteControls() throws IOException, InterruptedException {
        if (channel == null)
            return Collections.emptyList();

        Collection<SeleniumTestSlotGroup> rcs = channel.call(new Callable<Collection<SeleniumTestSlotGroup>, RuntimeException>() {

            /**
			 * 
			 */
            private static final long serialVersionUID = 1791985298575049757L;

            public Collection<SeleniumTestSlotGroup> call() throws RuntimeException {
                Map<URL, SeleniumTestSlotGroup> groups = new HashMap<URL, SeleniumTestSlotGroup>();

                Registry registry = RegistryHolder.registry;
                if (registry != null) {
                    for (RemoteProxy proxy : registry.getAllProxies()) {
                        for (TestSlot slot : proxy.getTestSlots()) {
                            URL host = slot.getProxy().getRemoteHost();
                            SeleniumTestSlotGroup grp = groups.get(host);
                            if (grp == null) {
                                grp = new SeleniumTestSlotGroup(host);
                                groups.put(host, grp);
                            }
                            grp.addTestSlot(new SeleniumTestSlot(slot));
                        }

                    }
                }
                List<SeleniumTestSlotGroup> values = new ArrayList<SeleniumTestSlotGroup>(groups.values());
                Collections.sort(values);
                return values;
            }
        });
        return rcs;

    }

    /**
     * Determines the host name of the Jenkins master.
     */
    public static String getMasterHostName() {
        return getPlugin().hostnameResolver.retrieveHost();
    }

    /**
     * Handles incremental log.
     */
    public void doProgressiveLog(StaplerRequest req, StaplerResponse rsp) throws IOException {
        new LargeText(getLogFile(), false).doProgressText(req, rsp);
    }

    @SuppressWarnings( "unchecked" )
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
        if (rcFirefoxProfileTemplate != null || rcBrowserSessionReuse != null || rcTrustAllSSLCerts != null || rcBrowserSideLog != null) {
            String rcFirefoxProfileTemplate_ = getDefaultForNull(rcFirefoxProfileTemplate, "");
            Boolean rcBrowserSessionReuse_ = getDefaultForNull(rcBrowserSessionReuse, Boolean.FALSE);
            Boolean rcTrustAllSSLCerts_ = getDefaultForNull(rcTrustAllSSLCerts, Boolean.FALSE);
            Boolean rcBrowserSideLog_ = getDefaultForNull(rcBrowserSideLog, Boolean.FALSE);

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

            SeleniumNodeConfiguration c = new CustomRCConfiguration(port_, rcBrowserSideLog_, rcDebug, rcTrustAllSSLCerts_, rcBrowserSessionReuse_,
                    -1, rcFirefoxProfileTemplate_, browsers, null);

            synchronized (configurations) {
                configurations.add(new SeleniumGlobalConfiguration("Selenium v2.0 configuration", new MatchAllMatcher(), c));
            }

        }

        // update to 2.3
        if (hostnameResolver == null) {
            hostnameResolver = new JenkinsRootHostnameResolver();
        }

        return this;
    }

    /**
     * Returns either the object, or the default value is null.
     * 
     * @param object
     *            Object to return
     * @param defaultObject
     *            Default value in case object is null
     * @return Object value
     */
    private <T> T getDefaultForNull(T object, T defaultObject) {
        return object == null ? defaultObject : object;
    }

    public static void startSeleniumNode(Computer c, TaskListener listener, String conf) throws IOException, InterruptedException {
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
            for (Label label : c.getNode().getAssignedLabels()) {
                for (String pattern : exclusionPatterns) {
                    if (label.toString().matches(pattern)) {
                        LOGGER.fine("Node " + c.getNode().getDisplayName() + " is excluded from Selenium Grid because its label '" + label
                                + "' matches exclusion pattern '" + pattern + "'");
                        return;
                    }
                }
            }
        }

        final String masterName = PluginImpl.getMasterHostName();
        if (masterName == null) {
            listener.getLogger().println(
                    "Unable to determine the host name of the master. Skipping Selenium execution. " + "Please "
                            + HyperlinkNote.encodeTo("/configure", "configure the Jenkins URL") + " from the system configuration screen.");
            return;
        }

        // make sure that Selenium Hub is started before we start RCs.
        try {
            p.waitForHubLaunch();
        } catch (ExecutionException e) {
            throw new IOException2("Failed to wait for the Hub launch to complete", e);
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

        listener.getLogger().println("Starting Selenium nodes on " + c.getName());

        for (SeleniumGlobalConfiguration config : confs) {
            if ((conf != null && config.getName().equals(conf)) || conf == null) {
                try {
                    config.start(c, listener);
                } catch (ExecutionException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
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

    public boolean hasGlobalConfiguration(String name) {
        return getConfiguration(name) != null;
    }

    public void removeGlobalConfigurations(String name) throws IOException {
        removeGlobalConfigurations(name, true);
    }

    public List<SeleniumGlobalConfiguration> getGlobalConfigurationForComputer(Computer computer) {
        List<SeleniumGlobalConfiguration> confs = new ArrayList<SeleniumGlobalConfiguration>();
        for (SeleniumGlobalConfiguration c : PluginImpl.getPlugin().getGlobalConfigurations()) {
            if (c.getMatcher().match(computer.getNode())) {
                confs.add(c);
            }
        }
        return confs;

    }

    public SeleniumGlobalConfiguration getConfiguration(String name) {
        for (SeleniumGlobalConfiguration c : configurations) {
            if (name.equals(c.getName())) {
                return c;
            }
        }
        return null;
    }

    /**
     * 
     * @param req
     *            StaplerRequest
     * @param rsp
     *            StaplerResponse to redirect with
     * @throws IOException
     *             if redirection goes wrong
     */
    public void doAddRedirect(StaplerRequest req, StaplerResponse rsp) throws IOException {
        Hudson.getInstance().checkPermission(getRequiredPermission());
        rsp.sendRedirect2("add");
    }

    /**
     * 
     * @param req
     *            StaplerRequest
     * @param rsp
     *            StaplerResponse to redirect with
     * @throws IOException
     *             if redirection goes wrong
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

    public DescriptorExtensionList<HostnameResolver, HostnameResolverDescriptor> getResolverTypes() {
        return HostnameResolver.all();
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

    public Channel getHubChannel() {
        return channel;
    }

    public SeleniumConfigurationMatcher getDefaultMatcher() {
        return new MatchAllMatcher();
    }

    public SeleniumNodeConfiguration getDefaultConfiguration() {
        List<WebDriverBrowser> browsers = new ArrayList<WebDriverBrowser>();
        browsers.add(new hudson.plugins.selenium.configuration.browser.webdriver.IEBrowser(1, null, null));
        browsers.add(new hudson.plugins.selenium.configuration.browser.webdriver.FirefoxBrowser(5, null, null));
        browsers.add(new hudson.plugins.selenium.configuration.browser.webdriver.ChromeBrowser(5, null, null));
        return new CustomWDConfiguration(4445, null, browsers, null);
    }

    /**
     * @param name
     * @param conf
     * @throws IOException
     */
    public void replaceGlobalConfigurations(String name, SeleniumGlobalConfiguration conf) throws IOException {
        removeGlobalConfigurations(name, false);
        configurations.add(conf);
        PluginImpl.getPlugin().save();
    }

    /**
     * @param name
     * @param save
     * @throws IOException
     */
    private void removeGlobalConfigurations(String name, boolean save) throws IOException {
        Iterator<SeleniumGlobalConfiguration> it = configurations.iterator();
        while (it.hasNext()) {
            SeleniumGlobalConfiguration conf = it.next();
            if (conf.getName().equals(name)) {
                it.remove();
                if (save) {
                    save();
                }

                // there should only be one config with that name
                return;
            }
        }
    }
}