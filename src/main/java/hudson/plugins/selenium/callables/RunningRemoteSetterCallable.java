package hudson.plugins.selenium.callables;

import hudson.plugins.selenium.RemoteRunningStatus;
import hudson.remoting.Callable;

/**
 * Callable that returns a property set on the channel
 * 
 * @author Richard Lavoie
 * 
 * @param <T>
 * @param <E>
 */
public class RunningRemoteSetterCallable implements Callable<Void, Exception> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3519905249359789575L;

	private String config;

	private String status;

	public RunningRemoteSetterCallable(String conf, String status) {
		config = conf;
		this.status = status;
	}

	public Void call() throws Exception {
		((RemoteRunningStatus) PropertyUtils.getProperty(
				SeleniumConstants.PROPERTY_STATUS).get(config))
				.setStatus(status);
		return null;
	}

}
