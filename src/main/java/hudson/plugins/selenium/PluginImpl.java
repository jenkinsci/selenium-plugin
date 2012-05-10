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

import hudson.Extension;
import hudson.FilePath;
import hudson.Plugin;
import hudson.model.Action;
import hudson.model.Api;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.TaskListener;
import hudson.plugins.selenium.configuration.Configuration;
import hudson.plugins.selenium.configuration.CustomConfiguration;
import hudson.plugins.selenium.configuration.browser.Browser;
import hudson.plugins.selenium.configuration.browser.ChromeBrowser;
import hudson.plugins.selenium.configuration.browser.FirefoxBrowser;
import hudson.plugins.selenium.configuration.browser.IEBrowser;
import hudson.remoting.Callable;
import hudson.remoting.Channel;
import hudson.remoting.Which;
import hudson.slaves.Channels;
import hudson.util.ClasspathBuilder;
import hudson.util.JVMBuilder;
import hudson.util.StreamTaskListener;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;

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

/**
 * Starts Selenium Grid server in another JVM.
 *
 * @author Kohsuke Kawaguchi
 */
@ExportedBean
public class PluginImpl extends Plugin implements Action, Serializable, Describable<PluginImpl> {

    private int port = 4444;
    private String exclusionPatterns;
    private Integer newSessionWaitTimeout = -1;
    private boolean throwOnCapabilityNotPresent = false;
    private String hubLogLevel = "INFO";
    private boolean rcDebug; 
    private String rcLog;

    /**
     * Channel to Selenium Grid JVM.
     */
    private transient Channel channel;

    private transient Future<?> hubLauncher;
    
    @Override
    public void postInitialize() throws Exception {
        load();
        
        StreamTaskListener listener = new StreamTaskListener(getLogFile());
        File root = Hudson.getInstance().getRootDir();
        channel = createSeleniumGridVM(root, listener);
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
        
        hubLauncher = channel.callAsync(new HubLauncher(port, args.toArray(new String[0]), logLevel));

        Hudson.getInstance().getActions().add(this);
    }


	public File getLogFile() {
        return new File(Hudson.getInstance().getRootDir(),"selenium.log");
    }

    @Override
    public void configure(StaplerRequest req, JSONObject formData) {
        port = formData.getInt("port");
        exclusionPatterns = formData.getString("exclusionPatterns");
        rcLog = formData.getString("rcLog");
        rcDebug = formData.getBoolean("rcDebug");
        newSessionWaitTimeout = formData.getInt("newSessionWaitTimeout");
        throwOnCapabilityNotPresent = formData.getBoolean("throwOnCapabilityNotPresent"); 
        hubLogLevel = formData.getString("hubLogLevel");
//        rcBrowserSideLog = formData.getBoolean("rcBrowserSideLog");
//        rcTrustAllSSLCerts = formData.getBoolean("rcTrustAllSSLCerts");
//        rcBrowserSessionReuse = formData.getBoolean("rcBrowserSessionReuse");
//        rcFirefoxProfileTemplate = formData.getString("rcFirefoxProfileTemplate");
        try {
            save();
        } catch (IOException e) {
            e.printStackTrace(); 
        }
    }

    public void waitForHubLaunch() throws ExecutionException, InterruptedException {
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
    public String getExclusionPatterns(){
        return exclusionPatterns;
    }

    @Exported
    public Integer getNewSessionWaitTimeout() {
    	return newSessionWaitTimeout;
    }
    
    @Exported
    public String getHubLogLevel(){
        return hubLogLevel != null ? hubLogLevel : "INFO";
    }
    
    @Exported
    public boolean getRcDebug(){
        return rcDebug;
    }
    
    @Exported
    public String getRcLog(){
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

    @Exported(inline=true)
    public List<SeleniumTestSlot> getRemoteControls() throws IOException, InterruptedException {
        if(channel==null)   return Collections.emptyList();

        return channel.call(new Callable<List<SeleniumTestSlot>,RuntimeException>() {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1791985298575049757L;

			public List<SeleniumTestSlot> call() throws RuntimeException {
                List<SeleniumTestSlot> r = new ArrayList<SeleniumTestSlot>();
                
                Registry registry = RegistryHolder.registry;
                if (registry!=null) {
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
     *      The slave/master root.
     */
    static /*package*/ Channel createSeleniumGridVM(File rootDir, TaskListener listener) throws IOException, InterruptedException {
    	
    	JVMBuilder vmb = new JVMBuilder();
    	//vmb.debug(8000);
    	vmb.systemProperties(null);
        return Channels.newJVM("Selenium Grid",listener,vmb, new FilePath(rootDir),
                new ClasspathBuilder().add(findStandAloneServerJar()));
    }

    /**
     * Locate the stand-alone server jar from the classpath. Only works on the master.
     */
    /*package*/ static File findStandAloneServerJar() throws IOException {
        return Which.jarFile(GridLauncher.class);
    }

    /**
     * Launches RC in a separate JVM.
     *
     * @param standaloneServerJar
     *      The jar file of the grid to launch.
     */
    static /*package*/ Channel createSeleniumRCVM(File standaloneServerJar, TaskListener listener, Map<String, String> properties) throws IOException, InterruptedException {
        return Channels.newJVM("Selenium RC",listener,null,
                new ClasspathBuilder().add(standaloneServerJar),
                properties);
    }

    /**
     * Determines the host name of the Jenkins master.
     */
    public static String getMasterHostName() throws MalformedURLException {
        String rootUrl = Hudson.getInstance().getRootUrl();
        if(rootUrl==null)
            return null;
        URL url = new URL(rootUrl);
        return url.getHost();
    }

    /**
     * Handles incremental log.
     */
    public void doProgressiveLog( StaplerRequest req, StaplerResponse rsp) throws IOException {
        new LargeText(getLogFile(),false).doProgressText(req,rsp);
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
    	
    	if (rcFirefoxProfileTemplate != null || rcBrowserSessionReuse != null || rcTrustAllSSLCerts != null || rcBrowserSideLog != null) {
    	    String rcFirefoxProfileTemplate_ = getDefaultForNull(rcFirefoxProfileTemplate, ""); 
    	    Boolean rcBrowserSessionReuse_ = getDefaultForNull(rcBrowserSessionReuse, Boolean.FALSE); 
    	    Boolean rcTrustAllSSLCerts_ = getDefaultForNull(rcTrustAllSSLCerts, Boolean.FALSE); 
    	    Boolean rcBrowserSideLog_ = getDefaultForNull(rcBrowserSideLog, Boolean.FALSE); 
    		
    	    
    	    List<Browser> browsers = new ArrayList<Browser>();
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
    	    
    	    Configuration c = new CustomConfiguration(port_, rcBrowserSideLog_, rcDebug, rcTrustAllSSLCerts_, rcBrowserSessionReuse_, -1, rcFirefoxProfileTemplate_, browsers);
    	    
    	    try {
				Hudson.getInstance().getGlobalNodeProperties().add(new NodePropertyImpl(c));
			} catch (IOException e) {
				e.printStackTrace();
			}
    	    
    	}
    	
    	return this;
    }
    
    private <T> T getDefaultForNull(T object, T defObject) {
    	return object == null ? defObject : object;
    }
    
    private transient String rcFirefoxProfileTemplate;
    private transient Boolean rcBrowserSessionReuse;
    private transient Boolean rcTrustAllSSLCerts;
    private transient Boolean rcBrowserSideLog;
}
