package hudson.plugins.selenium.callables;

import hudson.remoting.Callable;
import hudson.remoting.Channel;
import hudson.remoting.ChannelProperty;

import java.io.IOException;

public class DeepLevelCallable<V, T extends Exception> implements Callable<V, T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4335118073900964890L;
	private ChannelProperty<Channel> property;
	private Callable<V, T> call; 
	private V defaultValue = null;
	
	public DeepLevelCallable(ChannelProperty<Channel> property, Callable<V, T> callable) {
		this(property, callable, null);
	}
	
	public DeepLevelCallable(ChannelProperty<Channel> property, Callable<V, T> callable, V value) {
		call = callable;
		this.property = property;
		defaultValue = value;
	}

	public V call() throws T {
		
		try {
			Channel chan = PropertyUtils.getProperty(property);
			if (chan == null)
				return defaultValue;
			return chan.call(call);
		} catch (IOException e) {
		} catch (InterruptedException e) {
		}
		return null;
	}
	
}
