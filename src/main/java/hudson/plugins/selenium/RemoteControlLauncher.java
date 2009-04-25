package hudson.plugins.selenium;

import com.thoughtworks.selenium.grid.remotecontrol.SelfRegisteringRemoteControlLauncher;
import hudson.remoting.Callable;
import hudson.remoting.Which;

import java.util.Arrays;

import org.openqa.selenium.server.SeleniumServer;

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

    // because this method is called asynchronously and no one waits for the completion,
    // exception needs to be reproted explicitly.
    public Void call() throws Exception {
        try {
            System.out.println("Starting Selenium RC with "+ Arrays.asList(args));
            System.out.println(Which.jarFile(SeleniumServer.class));
            System.out.println(Which.jarFile(SelfRegisteringRemoteControlLauncher.class));
            SelfRegisteringRemoteControlLauncher.main(args);

            System.out.println("Blocking");
            // block forever
            Object o = new Object();
            synchronized (o) {
                o.wait();
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } catch (Error e) {
            e.printStackTrace();
            throw e;
        }
    }
}
