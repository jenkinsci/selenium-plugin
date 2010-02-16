package hudson.plugins.selenium;

import com.thoughtworks.selenium.grid.configuration.HubConfiguration;
import com.thoughtworks.selenium.grid.hub.HubRegistry;
import com.thoughtworks.selenium.grid.hub.HubServlet;
import com.thoughtworks.selenium.grid.hub.management.LifecycleManagerServlet;
import com.thoughtworks.selenium.grid.hub.management.RegistrationServlet;
import com.thoughtworks.selenium.grid.hub.management.UnregistrationServlet;
import com.thoughtworks.selenium.grid.hub.management.console.ConsoleServlet;
import hudson.remoting.Callable;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Starts the selenium grid server.
 *
 * This callable blocks until the server is shut down and thus generally never returns.
 *
 * @author Kohsuke Kawaguchi
 */
public class HubLauncher implements Callable<Void,Exception> {
    private final int port;
    private final Level logLevel;

    public HubLauncher(int port, Level logLevel) {
        this.port = port;
        this.logLevel = logLevel;
    }

    public Void call() throws Exception {
        HubRegistry r = HubRegistry.registry();
        // hack up the pool
        Field pool = r.getClass().getDeclaredField("pool");
        pool.setAccessible(true);
        pool.set(r,new HudsonRemoteControlPool());
        // and environment manager
        Field env = r.getClass().getDeclaredField("environmentManager");
        env.setAccessible(true);
        env.set(r,new HudsonEnvironmentManager());


        r.gridConfiguration().getHub().setPort(port);
        start();
        return null;
    }

    private void start() throws Exception {
        final ContextHandlerCollection contexts;
        final HubConfiguration configuration;
        final Server server;
        final Context root;


        configuration = HubRegistry.registry().gridConfiguration().getHub();
        server = new Server(configuration.getPort());

        contexts = new ContextHandlerCollection();
        server.setHandler(contexts);

        root = new Context(contexts, "/", Context.SESSIONS);
//        root.setResourceBase("./");
//        root.addHandler(new ResourceHandler());
        root.addServlet(new ServletHolder(new HubServlet()), "/selenium-server/driver/*");
        root.addServlet(new ServletHolder(new ConsoleServlet()), "/console");
        root.addServlet(new ServletHolder(new RegistrationServlet()), "/registration-manager/register");
        root.addServlet(new ServletHolder(new UnregistrationServlet()), "/registration-manager/unregister");
        root.addServlet(new ServletHolder(new LifecycleManagerServlet()), "/lifecycle-manager");

        server.start();

        Logger.getLogger("com.thoughtworks.selenium").setLevel(logLevel);
        Logger.getLogger("org.apache.commons.httpclient").setLevel(logLevel);
    }
}
