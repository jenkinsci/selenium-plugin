package hudson.plugins.selenium;

import com.thoughtworks.selenium.grid.remotecontrol.SelfRegisteringRemoteControlLauncher;
import hudson.remoting.Callable;

/**
 * Launches Selenium RC.
 *
 * <p>
 * This callable is run on the JVM dedicated to selenium RC.
 *
 * @author Kohsuke Kawaguchi
 */
public class RemoteControlLauncher implements Callable<Void,Exception> {
    private final String[] args;

    public RemoteControlLauncher(String... args) {
        this.args = args;
    }

    public Void call() throws Exception {
        SelfRegisteringRemoteControlLauncher.main(args);

        // block forever
        Object o = new Object();
        synchronized (o) {
            o.wait();
        }
        return null;
    }
}
