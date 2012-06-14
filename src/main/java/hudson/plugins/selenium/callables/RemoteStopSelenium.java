package hudson.plugins.selenium.callables;

import hudson.remoting.Callable;
import hudson.remoting.Channel;

import org.openqa.grid.internal.utils.SelfRegisteringRemote;

public class RemoteStopSelenium implements Callable<Void, Exception> {

	public Void call() throws Exception {
		PropertyUtils.getProperty(SeleniumConstants.PROPERTY_INSTANCE).stopRemoteServer();
		Object o = PropertyUtils.getProperty(SeleniumConstants.PROPERTY_LOCK);
		synchronized(o) {
			o.notifyAll();
		}
		return null;
	}

}
