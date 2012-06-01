package hudson.plugins.selenium.callables;

import hudson.remoting.Callable;
import hudson.remoting.Channel;

import org.openqa.grid.internal.utils.SelfRegisteringRemote;

public class RemoteStartSelenium implements Callable<Void, Exception> {

	public Void call() throws Exception {
		PropertyUtils.setProperty(SeleniumConstants.PROPERTY_STATUS, SeleniumConstants.STARTING);
		PropertyUtils.getProperty(SeleniumConstants.PROPERTY_INSTANCE).startRemoteServer();
		PropertyUtils.setProperty(SeleniumConstants.PROPERTY_STATUS, SeleniumConstants.STARTED);
		return null;
	}

}
