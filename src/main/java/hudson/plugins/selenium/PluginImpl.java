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
import com.thoughtworks.selenium.grid.hub.remotecontrol.DynamicRemoteControlPool;
import com.thoughtworks.selenium.grid.hub.remotecontrol.RemoteControlProxy;
import com.thoughtworks.selenium.grid.hub.management.RegistrationServlet;
import com.thoughtworks.selenium.grid.hub.management.UnregistrationServlet;
import com.thoughtworks.selenium.grid.hub.management.console.ConsoleServlet;
import hudson.Plugin;
import hudson.model.Descriptor.FormException;
import hudson.model.Api;
import hudson.model.Hudson;
import hudson.model.Action;
import net.sf.json.JSONObject;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.export.Exported;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

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
    private transient Server server;

    private int port = 4444;

    public void start() throws Exception {
        HubConfiguration configuration = ApplicationRegistry.registry().gridConfiguration().getHub();
        configuration.setPort(port);
        server = new Server(configuration.getPort());

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        server.setHandler(contexts);

        Context root = new Context(contexts, "/", Context.SESSIONS);
//        root.setResourceBase("./");
//        root.addHandler(new ResourceHandler());
        root.addServlet(new ServletHolder(new HubServlet()), "/selenium-server/driver/*");
        root.addServlet(new ServletHolder(new ConsoleServlet()), "/console");
        root.addServlet(new ServletHolder(new RegistrationServlet()), "/registration-manager/register");
        root.addServlet(new ServletHolder(new UnregistrationServlet()), "/registration-manager/unregister");

        server.start();

        Hudson.getInstance().getActions().add(this);
        
        new ComputerListenerImpl().register();
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
        server.stop();
        server.join();
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
}
