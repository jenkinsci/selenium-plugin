package hudson.plugins.selenium.callables;

import hudson.plugins.selenium.SeleniumRunOptions;
import hudson.remoting.Callable;

import java.util.Map;

public class GetConfigurations implements Callable<Map<String, SeleniumRunOptions>, Exception> {

	@SuppressWarnings("unchecked")
	public Map<String, SeleniumRunOptions> call() throws Exception {
		return PropertyUtils.getProperty(SeleniumConstants.PROPERTY_STATUS);
	}

}
