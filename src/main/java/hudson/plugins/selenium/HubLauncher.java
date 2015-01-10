package hudson.plugins.selenium;

import hudson.remoting.Channel;

import java.util.logging.Level;
import java.util.logging.Logger;

import jenkins.security.MasterToSlaveCallable;

import org.jfree.util.Log;
import org.openqa.grid.internal.utils.GridHubConfiguration;
import org.openqa.grid.web.Hub;

/**
 * Starts the selenium grid server.
 *
 * This callable blocks until the server is shut down and thus generally never returns.
 *
 * @author Kohsuke Kawaguchi
 */
public class HubLauncher extends MasterToSlaveCallable<Void, Exception> {

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

    public Void call() {
        try {
            Logger LOG = Logger.getLogger(HubLauncher.class.getName());
            configureLoggers();
            LOG.info("Grid Hub preparing to start on port " + port);
            GridHubConfiguration c = GridHubConfiguration.build(args);
            c.setPort(port);
            c.setCapabilityMatcher(new JenkinsCapabilityMatcher(Channel.current(), c.getCapabilityMatcher()));
            Hub hub = new Hub(c);
            hub.start();
            HubHolder.hub = hub;

            StringBuilder arguments = new StringBuilder();
            for (String arg : args) {
                arguments.append(" ").append(arg);
            }
            LOG.info("Grid Hub started on port " + port + " with args:" + arguments.toString());
        } catch (Exception e) {
            Log.error("An error occurred while starting the hub", e);
        }

        return null;
    }

    private void configureLoggers() {
        Logger.getLogger("org.openqa").setLevel(logLevel);
        Logger.getLogger("org.seleniumhq").setLevel(logLevel);
        Logger.getLogger("com.thoughtworks.selenium").setLevel(logLevel);
        Logger.getLogger("org.apache.commons.httpclient").setLevel(logLevel);
    }
}
