package hudson.plugins.selenium.callables;

import hudson.remoting.Callable;
import hudson.remoting.ChannelProperty;

public class ChannelPropertySetterCallable<T> implements Callable<Void,Exception> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3519905249359789575L;
	
	private ChannelProperty<T> prop;
	private T val;

	public ChannelPropertySetterCallable(ChannelProperty<T> property, T value) {
		prop = property;
		val = value;
	}

	public Void call() throws Exception {
		PropertyUtils.setProperty(prop, val);
		return null;
	}

	
	
}
