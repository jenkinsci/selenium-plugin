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

import com.thoughtworks.selenium.grid.hub.HubRegistry;
import com.thoughtworks.selenium.grid.hub.remotecontrol.DynamicRemoteControlPool;
import com.thoughtworks.selenium.grid.hub.remotecontrol.RemoteControlProxy;
import hudson.FilePath;
import hudson.Plugin;
import hudson.model.Action;
import hudson.model.Api;
import hudson.model.Hudson;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import hudson.remoting.Channel;
import hudson.slaves.Channels;
import hudson.util.ClasspathBuilder;
import hudson.util.StreamTaskListener;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.framework.io.LargeText;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;

/**
 * Starts Selenium Grid server in another JVM.
 *
 * @author Kohsuke Kawaguchi
 */
@ExportedBean
public class PluginImpl extends Plugin implements Action, Serializable {

    private int port = 4444;
    private String exclusionPatterns;
    private boolean rcBrowserSideLog;
    private boolean rcDebug;
    private boolean rcTrustAllSSLCerts;
    private String rcFirefoxProfileTemplate;
    private String rcLog;
    private String hubLogLevel = "INFO";

    /**
     * Channel to Selenium Grid JVM.
     */
    private transient Channel channel;

    private transient Future<?> hubLauncher;

    public void start() throws Exception {
        load();
        StreamTaskListener listener = new StreamTaskListener(getLogFile());
        File root = Hudson.getInstance().getRootDir();
        channel = createSeleniumGridVM(root, listener);
        Level logLevel = hubLogLevel != null ? Level.parse(hubLogLevel) : Level.INFO;
        hubLauncher = channel.callAsync(new HubLauncher(port, logLevel));

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
        rcBrowserSideLog = formData.getBoolean("rcBrowserSideLog");
        rcTrustAllSSLCerts = formData.getBoolean("rcTrustAllSSLCerts");
        rcFirefoxProfileTemplate = formData.getString("rcFirefoxProfileTemplate");
        hubLogLevel = formData.getString("hubLogLevel");
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
    public String getRcLog(){
        return rcLog;
    }

    @Exported
    public boolean getRcBrowserSideLog(){
        return rcBrowserSideLog;
    }

    @Exported
    public boolean getRcDebug(){
        return rcDebug;
    }

    @Exported
    public boolean getRcTrustAllSSLCerts() {
        return rcTrustAllSSLCerts;
    }

    @Exported
    public String getRcFirefoxProfileTemplate(){
        return rcFirefoxProfileTemplate;
    }

    @Exported
    public String getHubLogLevel(){
        return hubLogLevel != null ? hubLogLevel : "INFO";
    }

    public void stop() throws Exception {
        channel.close();
    }

    @Exported(inline=true)
    public List<SeleniumRemoteControl> getRemoteControls() throws IOException, InterruptedException {
        if(channel==null)   return Collections.emptyList();

        return channel.call(new Callable<List<SeleniumRemoteControl>,RuntimeException>() {
            public List<SeleniumRemoteControl> call() throws RuntimeException {
                HubRegistry registry = HubRegistry.registry();
                DynamicRemoteControlPool pool = registry.remoteControlPool();

                List<SeleniumRemoteControl> r = new ArrayList<SeleniumRemoteControl>();
                for (RemoteControlProxy rc : pool.availableRemoteControls())
                    r.add(new SeleniumRemoteControl(rc,false));
                for (RemoteControlProxy rc : pool.reservedRemoteControls())
                    r.add(new SeleniumRemoteControl(rc,true));

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
        FilePath distDir = install(rootDir, listener);
        return Channels.newJVM("Selenium Grid",listener,distDir,
                new ClasspathBuilder().addAll(distDir,"lib/selenium-grid-hub-standalone-*.jar, lib/log4j-*.jar"),
                null);
    }

    /**
     * Launches RC in a separate JVM.
     *
     * @param rootDir
     *      The slave/master root.
     */
    static /*package*/ Channel createSeleniumRCVM(File rootDir, TaskListener listener) throws IOException, InterruptedException {
        FilePath distDir = install(rootDir, listener);
        return Channels.newJVM("Selenium RC",listener,distDir,
                new ClasspathBuilder()
                        .addAll(distDir,"vendor/selenium-server-*.jar, lib/selenium-grid-remote-control-*.jar, lib/commons-httpclient-*.jar"),
                null);
    }

    private static FilePath install(File rootDir, TaskListener listener) throws IOException, InterruptedException {
        FilePath distDir = new FilePath(new File(rootDir,"selenium-grid"));
        distDir.installIfNecessaryFrom(PluginImpl.class.getResource("selenium-grid.tgz"),listener,"Installing Selenium Grid binaries");
        return distDir;
    }

    /**
     * Determines the host name of the Jenkins master.
     */
    static /*package*/ String getMasterHostName() throws MalformedURLException {
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

    private static final long serialVersionUID = 1L;
}
