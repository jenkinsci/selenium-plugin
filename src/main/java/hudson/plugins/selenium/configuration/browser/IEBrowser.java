package hudson.plugins.selenium.configuration.browser;

import hudson.Extension;
import hudson.model.Descriptor;

import java.util.List;

public class IEBrowser extends Browser {
	
    @Extension
    public static class DescriptorImpl extends BrowserDescriptor {

	
	@Override
	public String getDisplayName() {
		return "Internet Explorer";
	}
	
	@Override
	public List<String> getArgs() {
		return null;
	}
	
    }
}
