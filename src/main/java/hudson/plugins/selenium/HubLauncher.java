package hudson.plugins.selenium;

import hudson.remoting.Callable;

import java.io.IOException;

import com.thoughtworks.selenium.grid.configuration.HubConfiguration;
import com.thoughtworks.selenium.grid.hub.ApplicationRegistry;
import com.thoughtworks.selenium.grid.hub.HubServlet;
import com.thoughtworks.selenium.grid.hub.management.console.ConsoleServlet;
import com.thoughtworks.selenium.grid.hub.management.RegistrationServlet;
import com.thoughtworks.selenium.grid.hub.management.UnregistrationServlet;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.handler.ContextHandlerCollection;

/**
 * Starts the selenium grid server.
 *
 * @author Kohsuke Kawaguchi
 */
public class HubLauncher implements Callable<Void,Exception> {
    public HubLauncher(int port) {
        this.port = port;
    }

    public Void call() throws Exception {
        HubConfiguration configuration = ApplicationRegistry.registry().gridConfiguration().getHub();
        configuration.setPort(port);

        Server server = new Server(configuration.getPort());

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

        return null;
    }
}
