package hudson.plugins.selenium.configuration.browser;

import hudson.Extension;

import java.util.List;

@Extension
public class IEBrowser extends Browser {
	
	@Override
	public List<String> getArgs() {
		return null;
	}
}
