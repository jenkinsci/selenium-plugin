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
public class CloseSeleniumChannelCallable implements Callable<Void,Exception> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3519905249359789575L;
	
	private String config;

	public CloseSeleniumChannelCallable(String conf) {
		config = conf;
	}

	public Void call() throws Exception {
		RemoteRunningStatus st = (RemoteRunningStatus) PropertyUtils.getProperty(SeleniumConstants.PROPERTY_STATUS).get(config);
		st.getSeleniumChannel().close();
		return null;
	}

	
	
}
