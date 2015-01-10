package hudson.plugins.selenium;

import hudson.plugins.selenium.callables.PropertyUtils;
import hudson.plugins.selenium.callables.SeleniumConstants;
import hudson.remoting.Channel;
import jenkins.security.MasterToSlaveCallable;

import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.internal.utils.SelfRegisteringRemote;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Launches Selenium RC.
 * 
 * <p>
 * This callable is run on the JVM dedicated to selenium RC.
 * 
 * @author Kohsuke Kawaguchi
 * @author Richard Lavoie
 */
public class RemoteControlLauncher extends MasterToSlaveCallable<Void, Exception> {

    /**
	 * 
	 */
    private static final long serialVersionUID = -6502768962889139192L;

    private final String[] args;
    private final String nodeName;

    public RemoteControlLauncher(String nodeName, String[] args) {
        this.nodeName = nodeName;
        this.args = args;
    }

    // because this method is called asynchronously and no one waits for the completion,
    // exception needs to be reported explicitly.
    public Void call() throws Exception {
        try {
            RegistrationRequest c = RegistrationRequest.build(args);
            for (DesiredCapabilities dc : c.getCapabilities()) {
                JenkinsCapabilityMatcher.enhanceCapabilities(dc, nodeName);
            }
            SelfRegisteringRemote remote = new SelfRegisteringRemote(c);
            PropertyUtils.setProperty(SeleniumConstants.PROPERTY_INSTANCE, remote);
            remote.startRemoteServer();
            remote.startRegistrationProcess();

            Channel.current().waitForProperty(SeleniumConstants.PROPERTY_LOCK);
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
