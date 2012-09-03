package hudson.plugins.selenium.callables;

import hudson.plugins.selenium.RemoteRunningStatus;
import hudson.remoting.Callable;

/**
 * Callable that returns a property set on the channel
 * @author Richard Lavoie
 *
 * @param <T>
 * @param <E>
 */
public class RunningRemoteGetterCallable implements Callable<RemoteRunningStatus,Exception> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3519905249359789575L;
	
	private String config;

	public RunningRemoteGetterCallable(String conf) {
		config = conf;
	}

	public RemoteRunningStatus call() throws Exception {
		return (RemoteRunningStatus) PropertyUtils.getProperty(SeleniumConstants.PROPERTY_STATUS).get(config);
	}

	
	
}
