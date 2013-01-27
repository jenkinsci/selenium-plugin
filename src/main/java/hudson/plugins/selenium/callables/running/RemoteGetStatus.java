package hudson.plugins.selenium.callables.running;

import hudson.plugins.selenium.RemoteRunningStatus;
import hudson.plugins.selenium.callables.PropertyUtils;
import hudson.plugins.selenium.callables.SeleniumConstants;
import hudson.remoting.Callable;

public class RemoteGetStatus implements Callable<String, Exception> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4826936469266107568L;

	private String config;
	
	public RemoteGetStatus(String conf) {
		config = conf;
	}

	public String call() throws Exception {
		return ((RemoteRunningStatus) PropertyUtils.getProperty(SeleniumConstants.PROPERTY_STATUS).get(config)).getStatus();
	}

}
