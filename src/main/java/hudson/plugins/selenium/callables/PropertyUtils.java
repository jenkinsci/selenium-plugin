package hudson.plugins.selenium.callables;

import hudson.remoting.Channel;
import hudson.remoting.ChannelProperty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PropertyUtils {

	private static Map<String, Object> properties = new ConcurrentHashMap<String, Object>();
	
	public static <T> T getProperty(ChannelProperty<T> property) {
		if (Channel.current() != null) {
			return Channel.current().getProperty(property);
		} else {
			return property.type.cast(properties.get(property.displayName));
		}
	}

	public static void setProperty(ChannelProperty<? extends Object> property, Object object) {
		if (Channel.current() != null) {
			Channel.current().setProperty(property, object);
		} else {
			properties.put(property.displayName, object);
		}
	}
	
}
