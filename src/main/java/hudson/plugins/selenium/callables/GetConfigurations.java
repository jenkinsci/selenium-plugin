package hudson.plugins.selenium.callables;

import hudson.plugins.selenium.SeleniumRunOptions;
import hudson.remoting.Callable;

import java.util.Map;

public class GetConfigurations implements Callable<Map<String, SeleniumRunOptions>, Exception> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8710676054398822727L;

	@SuppressWarnings("unchecked")
	public Map<String, SeleniumRunOptions> call() throws Exception {
		return PropertyUtils.getProperty(SeleniumConstants.PROPERTY_STATUS);
	}

}
