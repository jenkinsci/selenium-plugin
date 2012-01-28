package hudson.plugins.selenium.configuration.browser;

import hudson.Extension;
import hudson.model.Descriptor;

import java.util.List;

@Extension
public class FirefoxBrowser extends Browser {
	
	
    @Extension
    public static class DescriptorImpl extends BrowserDescriptor {

	@Override
	public String getDisplayName() {
		return "Firefox";
	}

	@Override
	public List<String> getArgs() {
		return null;
	}

	
    }
}
