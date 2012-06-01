package hudson.plugins.selenium.callables;

import hudson.remoting.Callable;

public class RemoteGetStatus implements Callable<String, Exception> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4826936469266107568L;

	public String call() throws Exception {
		return PropertyUtils.getProperty(SeleniumConstants.PROPERTY_STATUS);
	}

}
