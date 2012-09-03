package hudson.plugins.selenium.callables;

import hudson.remoting.ChannelProperty;

import java.util.Map;

import org.openqa.grid.internal.utils.SelfRegisteringRemote;

public class SeleniumConstants {
	
	@SuppressWarnings("rawtypes")
	public static final ChannelProperty<Map> PROPERTY_STATUS = new ChannelProperty<Map>(Map.class, "selenium.status");
	
	public static final ChannelProperty<Object> PROPERTY_LOCK = new ChannelProperty<Object>(Object.class, "selenium.lock");
	
	public static final ChannelProperty<SelfRegisteringRemote> PROPERTY_INSTANCE = new ChannelProperty<SelfRegisteringRemote>(SelfRegisteringRemote.class, "instance");
	
	public static final String STARTING = "Starting";
	public static final String STARTED = "Started";
	public static final String RESTARTING = "Restarting";
	public static final String STOPPING = "Stopping";
	public static final String STOPPED = "Stopped";

	public static final String ERROR = "Error";
}
