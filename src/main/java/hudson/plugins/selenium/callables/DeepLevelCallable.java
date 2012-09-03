package hudson.plugins.selenium.callables;

import hudson.plugins.selenium.RemoteRunningStatus;
import hudson.remoting.Callable;

public class DeepLevelCallable<V> implements Callable<V, Exception> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4335118073900964890L;
	private Callable<V, ? extends Exception> call; 
	private V defaultValue = null;
	private String config;
	
	public DeepLevelCallable(String conf, Callable<V, ? extends Exception> callable) {
		this(conf, callable, null);
	}
	
	public DeepLevelCallable(String conf, Callable<V, ? extends Exception> callable, V value) {
		call = callable;
		defaultValue = value;
		config = conf;
	}

	public V call() throws Exception {
        RemoteRunningStatus opt = (RemoteRunningStatus) PropertyUtils.getProperty(SeleniumConstants.PROPERTY_STATUS).get(config);
        if (opt== null)
            return defaultValue;
        return opt.getSeleniumChannel().call(call);
	}
	
}
