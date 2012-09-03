package hudson.plugins.selenium;

import hudson.plugins.selenium.callables.SeleniumConstants;
import hudson.remoting.Channel;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;

public class RemoteRunningStatus implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5791953423035014104L;
	
	private AtomicBoolean running = new AtomicBoolean(false);
	
	transient private Channel jvmChannel;
	
	private SeleniumRunOptions options;
	
	private String currentStatus;
	
	public RemoteRunningStatus(Channel channel, SeleniumRunOptions options) {
		jvmChannel = channel;
		this.options = options;
		currentStatus = SeleniumConstants.STARTING;
	}
	
	public boolean isRunning() {
		return running.get();
	}
	
	public void setRunning(boolean val) {
		running.set(val);
	}

	public void start() {
		running.set(true);
	}
	
	public SeleniumRunOptions getOptions() {
		return options;
	}
	
	public Channel getSeleniumChannel() {
		return jvmChannel;
	}
	
	public void setStatus(String status) {
		currentStatus = status;
	}
	
	public String getStatus() {
		return currentStatus;
	}
	
}
