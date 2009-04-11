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

import com.thoughtworks.selenium.grid.hub.HubServer;
import com.thoughtworks.selenium.grid.hub.HubRegistry;
import com.thoughtworks.selenium.grid.hub.remotecontrol.RemoteControlProxy;
import com.thoughtworks.selenium.grid.hub.remotecontrol.DynamicRemoteControlPool;
import hudson.FilePath;
import hudson.Plugin;
import hudson.model.Action;
import hudson.model.Api;
import hudson.model.Descriptor.FormException;
import hudson.model.Hudson;
import hudson.model.TaskListener;
import hudson.remoting.Channel;
import hudson.remoting.Callable;
import hudson.slaves.Channels;
import hudson.util.ClasspathBuilder;
import hudson.util.StreamTaskListener;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 * Starts Selenium Grid server in the same JVM.
 *
 * <p>
 * This means we are loading Jetty in another servlet container, which
 * is probably not a very robust thing to do.
 *
 * OTOH, Selenium requires to be deployed in the root context, so
 * we this seemed like the only practical way of doing this.
 *
 * <p>
 * The initialization code is taken from {@link HubServer} and hence licensed under ASL.
 *
 * TODO: wait until 1.245 to introduce persistence.
 *
 * @author Kohsuke Kawaguchi
 */
@ExportedBean
public class PluginImpl extends Plugin implements Action, Serializable {
    private int port = 4444;

    /**
     * Channel to Selenium Grid JVM.
     */
    private transient Channel channel;

    public void start() throws Exception {
        StreamTaskListener listener = new StreamTaskListener(System.out);
        File root = Hudson.getInstance().getRootDir();
        channel = createSeleniumGridVM(root, listener);
        channel.callAsync(new HubLauncher(port));

        Hudson.getInstance().getActions().add(this);
    }

    public String getIconFileName() {
        return "/plugin/selenium/24x24/selenium.png";
    }

    public String getDisplayName() {
        return "Selenium Grid";
    }

    public String getUrlName() {
        return "selenium";
    }

    public Api getApi() {
        return new Api(this);
    }

    @Exported
    public int getPort() {
        return port;
    }

    public void stop() throws Exception {
        channel.close();
    }

    public void configure(JSONObject formData) throws IOException, ServletException, FormException {
        formData.getString("port");
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
                new ClasspathBuilder().addAll(distDir,"*/lib/selenium-grid-hub-standalone-*.jar, */lib/log4j.jar"),
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
                        .addAll(distDir,"*/vendor/selenium-server-*.jar")
                        .addAll(distDir,"*/lib/selenium-grid-remote-control-standalone-*.jar"),
                null);
    }

    private static FilePath install(File rootDir, TaskListener listener) throws IOException, InterruptedException {
        FilePath distDir = new FilePath(new File(rootDir,"selenium-grid"));
        distDir.installIfNecessaryFrom(PluginImpl.class.getResource("selenium-grid.tgz"),listener,"Installing Selenium Grid binaries");
        return distDir;
    }

    private static final long serialVersionUID = 1L;
}
