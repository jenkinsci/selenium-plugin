package hudson.plugins.selenium.configuration.browser;

import hudson.Extension;

import java.util.List;

@Extension
public class ChromeBrowser extends Browser {
	
	@Override
	public List<String> getArgs() {
		return null;
	}
}
