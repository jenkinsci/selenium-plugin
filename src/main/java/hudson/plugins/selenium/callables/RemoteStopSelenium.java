package hudson.plugins.selenium.callables;

import hudson.remoting.Callable;
import hudson.remoting.Channel;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.internal.utils.SelfRegisteringRemote;

public class RemoteStopSelenium implements Callable<String, Exception> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5448989386458342771L;

	
	private String config;
	
	public RemoteStopSelenium(String conf) {
		config = conf;
	}

	public String call() throws Exception {
		Logger LOG = Logger.getLogger(SelfRegisteringRemote.class.getName());
		Logger.getLogger("org").setLevel(Level.ALL);
		LOG.fine("instances " + PropertyUtils.getProperty(SeleniumConstants.PROPERTY_INSTANCE));
		SelfRegisteringRemote srr = PropertyUtils
				.getProperty(SeleniumConstants.PROPERTY_INSTANCE);
		String url = getRemoteURL(srr);
		srr.stopRemoteServer();

        Channel.current().setProperty(SeleniumConstants.PROPERTY_LOCK, new Object());
		//LOG.fine("locks " + PropertyUtils.getProperty(SeleniumConstants.PROPERTY_LOCK));
		//Object o = PropertyUtils.getProperty(SeleniumConstants.PROPERTY_LOCK);
		//synchronized (o) {
//			o.notifyAll();
		//}
		return url;
	}

	private String getRemoteURL(SelfRegisteringRemote srr) {
		String host = (String) srr.getConfiguration().get(
				RegistrationRequest.HOST);
		Integer port = (Integer) srr.getConfiguration().get(
				RegistrationRequest.PORT);
		return "http://" + host + ":" + port;
	}

}
