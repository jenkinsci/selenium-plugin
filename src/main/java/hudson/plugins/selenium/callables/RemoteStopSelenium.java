package hudson.plugins.selenium.callables;

import hudson.remoting.Callable;

public class RemoteStopSelenium implements Callable<Void, Exception> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5448989386458342771L;

	public Void call() throws Exception {
		PropertyUtils.getProperty(SeleniumConstants.PROPERTY_INSTANCE).stopRemoteServer();
		Object o = PropertyUtils.getProperty(SeleniumConstants.PROPERTY_LOCK);
		synchronized(o) {
			o.notifyAll();
		}
		return null;
	}

}
