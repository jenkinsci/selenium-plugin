package hudson.plugins.selenium;

import hudson.remoting.Callable;

import java.io.IOException;

import com.thoughtworks.selenium.grid.configuration.HubConfiguration;
import com.thoughtworks.selenium.grid.hub.ApplicationRegistry;
import com.thoughtworks.selenium.grid.hub.HubServlet;
import com.thoughtworks.selenium.grid.hub.HubServer;
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
    private int port;

    public HubLauncher(int port) {
        this.port = port;
    }

    public Void call() throws Exception {
        // this method blocks until the system is shut down
        HubServer.main(new String[0]);

        return null;
    }
}
