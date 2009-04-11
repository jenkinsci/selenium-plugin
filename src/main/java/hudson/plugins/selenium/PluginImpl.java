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

import com.thoughtworks.selenium.grid.configuration.HubConfiguration;
import com.thoughtworks.selenium.grid.hub.ApplicationRegistry;
import com.thoughtworks.selenium.grid.hub.HubServer;
import com.thoughtworks.selenium.grid.hub.HubServlet;
import com.thoughtworks.selenium.grid.hub.management.RegistrationServlet;
import com.thoughtworks.selenium.grid.hub.management.UnregistrationServlet;
import com.thoughtworks.selenium.grid.hub.management.console.ConsoleServlet;
import com.thoughtworks.selenium.grid.hub.remotecontrol.DynamicRemoteControlPool;
import com.thoughtworks.selenium.grid.hub.remotecontrol.RemoteControlProxy;
import hudson.FilePath;
import hudson.Launcher.LocalLauncher;
import hudson.Plugin;
import hudson.Proc;
import hudson.model.Action;
import hudson.model.Api;
import hudson.model.Computer;
import hudson.model.Descriptor.FormException;
import hudson.model.Hudson;
import hudson.model.TaskListener;
import hudson.remoting.Channel;
import hudson.remoting.Which;
import hudson.slaves.Channels;
import hudson.util.ArgumentListBuilder;
import hudson.util.StreamTaskListener;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import javax.servlet.ServletException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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
public class PluginImpl extends Plugin implements Action {
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

    public ApplicationRegistry getRegistry() {
        return ApplicationRegistry.registry();
    }

    @Exported(inline=true)
    public List<SeleniumRemoteControl> getRemoteControls() {
        DynamicRemoteControlPool pool = getRegistry().remoteControlPool();

        List<SeleniumRemoteControl> r = new ArrayList<SeleniumRemoteControl>();
        for (RemoteControlProxy rc : pool.availableRemoteControls())
            r.add(new SeleniumRemoteControl(rc,false));
        for (RemoteControlProxy rc : pool.reservedRemoteControls())
            r.add(new SeleniumRemoteControl(rc,true));
        
        return r;
    }

    /**
     * Launches Hub in a separate JVM.
     *
     * @param rootDir
     *      The slave/master root.
     */
    static /*package*/ Channel createSeleniumGridVM(File rootDir, TaskListener listener) throws IOException, InterruptedException {
        FilePath distDir = new FilePath(new File(rootDir,"selenium-grid"));
        distDir.installIfNecessaryFrom(PluginImpl.class.getResource("selenium-grid.tar.gz"),listener,"Installing Selenium Grid binaries");

        // launch Hadoop in a new JVM and have them connect back to us
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(null);
        serverSocket.setSoTimeout(10*1000);

        ArgumentListBuilder args = new ArgumentListBuilder();
        args.add(new File(System.getProperty("java.home"),"bin/java"));
        args.add("-jar").add(Which.jarFile(Channel.class));

        // build up a classpath
        ClasspathBuilder classpath = new ClasspathBuilder();
        classpath.add(distDir,"*/lib/selenium-grid-hub-standalone-*.jar");
        args.add("-cp").add(classpath);

        args.add("-connectTo","localhost:"+serverSocket.getLocalPort());

        listener.getLogger().println("Starting Hadoop");
        Proc p = new LocalLauncher(listener).launch(args.toCommandArray(), new String[0], listener.getLogger(), null);

        Socket s = serverSocket.accept();
        serverSocket.close();

        return Channels.forProcess("Channel to Hadoop", Computer.threadPoolForRemoting,
                new BufferedInputStream(s.getInputStream()), new BufferedOutputStream(s.getOutputStream()), p);
    }
}
