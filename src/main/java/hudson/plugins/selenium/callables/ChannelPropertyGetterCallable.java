package hudson.plugins.selenium.callables;

import hudson.remoting.Callable;
import hudson.remoting.ChannelProperty;

public class ChannelPropertyGetterCallable<T,E extends Exception> implements Callable<T,E> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3519905249359789575L;
	
	private ChannelProperty<T> prop;

	public ChannelPropertyGetterCallable(ChannelProperty<T> property) {
		prop = property;
	}

	public T call() throws E {
		return PropertyUtils.getProperty(prop);
	}

	
	
}
