package hudson.plugins.selenium;

import com.thoughtworks.selenium.grid.hub.HubRegistry;
import com.thoughtworks.selenium.grid.hub.HubServer;
import hudson.remoting.Callable;

/**
 * Starts the selenium grid server.
 *
 * This callable blocks until the server is shut down and thus generally never returns.
 *
 * @author Kohsuke Kawaguchi
 */
public class HubLauncher implements Callable<Void,Exception> {
    private int port;

    public HubLauncher(int port) {
        this.port = port;
    }

    public Void call() throws Exception {
        HubRegistry.registry().gridConfiguration().getHub().setPort(port);
        HubServer.main(new String[0]);
        return null;
    }
}
