package hudson.plugins.selenium.callables;

import hudson.remoting.Channel;

import java.util.logging.Level;
import java.util.logging.Logger;

import jenkins.security.MasterToSlaveCallable;

import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.internal.utils.SelfRegisteringRemote;

public class RemoteStopSelenium extends MasterToSlaveCallable<String, Exception> {

    /**
	 * 
	 */
    private static final long serialVersionUID = -5448989386458342771L;

    public String call() throws Exception {
        Logger log = Logger.getLogger(SelfRegisteringRemote.class.getName());

        Logger.getLogger("org").setLevel(Level.ALL);

        log.fine("instances " + PropertyUtils.getProperty(SeleniumConstants.PROPERTY_INSTANCE));
        SelfRegisteringRemote srr = PropertyUtils.getProperty(SeleniumConstants.PROPERTY_INSTANCE);
        String url = getRemoteURL(srr);
        srr.stopRemoteServer();

        Channel.current().setProperty(SeleniumConstants.PROPERTY_LOCK, new Object());
        return url;
    }

    private String getRemoteURL(SelfRegisteringRemote srr) {
        String host = (String) srr.getConfiguration().get(RegistrationRequest.HOST);
        Integer port = (Integer) srr.getConfiguration().get(RegistrationRequest.PORT);
        return "http://" + host + ":" + port;
    }

}
