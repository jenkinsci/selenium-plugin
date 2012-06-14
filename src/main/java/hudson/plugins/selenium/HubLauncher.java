package hudson.plugins.selenium;

import hudson.remoting.Callable;
import hudson.remoting.Channel;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.grid.internal.utils.GridHubConfiguration;
import org.openqa.grid.web.Hub;

/**
 * Starts the selenium grid server.
 *
 * This callable blocks until the server is shut down and thus generally never returns.
 *
 * @author Kohsuke Kawaguchi
 */
public class HubLauncher implements Callable<Void,Exception> {
    /**
	 * 
	 */
	private static final long serialVersionUID = 5658971914841423874L;
	
	private final int port;
    private final String[] args;
    private final Level logLevel;

    public HubLauncher(int port, String[] args, Level logLevel) {
        this.port = port;
        this.args = args;
        this.logLevel = logLevel;
    }

    public Void call() throws Exception {
        configureLoggers();
        System.out.println("Grid Hub preparing to start on port "+port);
        GridHubConfiguration c = GridHubConfiguration.build(args);
        c.setPort(port);  
        c.setCapabilityMatcher(new JenkinsCapabilityMatcher(Channel.current(), c.getCapabilityMatcher()));
        Hub hub = new Hub(c);
        hub.start();
        RegistryHolder.registry = hub.getRegistry();
        System.out.println("Grid Hub started on port "+port);
        
//        HubRegistry r = HubRegistry.registry();
//        // hack up the pool
//        Field pool = r.getClass().getDeclaredField("pool");
//        pool.setAccessible(true);
//        pool.set(r,new HudsonRemoteControlPool());
//        // and environment manager
//        Field env = r.getClass().getDeclaredField("environmentManager");
//        env.setAccessible(true);
//        env.set(r,new HudsonEnvironmentManager());

        return null;
    }

    private void configureLoggers() {
        Logger.getLogger("org.openqa").setLevel(logLevel);
        Logger.getLogger("org.seleniumhq").setLevel(logLevel);
        Logger.getLogger("com.thoughtworks.selenium").setLevel(logLevel);
        Logger.getLogger("org.apache.commons.httpclient").setLevel(logLevel);
    }
}
