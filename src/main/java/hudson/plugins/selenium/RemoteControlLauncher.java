package hudson.plugins.selenium;

import hudson.remoting.Callable;
import hudson.remoting.Which;
import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.internal.utils.SelfRegisteringRemote;
import org.openqa.selenium.server.SeleniumServer;

import java.util.Arrays;
import java.util.Collections;

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
    private final String nodeName;

    public RemoteControlLauncher(String nodeName, String[] args) {
        this.nodeName = nodeName;
        this.args = args;
    }

    // because this method is called asynchronously and no one waits for the completion,
    // exception needs to be reproted explicitly.
    public Void call() throws Exception {
        try {
            System.out.println("Starting Selenium RC with "+ Arrays.asList(args));
            System.out.println(Which.jarFile(SeleniumServer.class));

            RegistrationRequest c = RegistrationRequest.build(args);
            c.addDesiredCapability(Collections.singletonMap(JenkinsCapabilityMatcher.NODE_NAME,(Object)nodeName));
            SelfRegisteringRemote remote = new SelfRegisteringRemote(c);
            remote.startRemoteServer();
            remote.startRegistrationProcess();

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
