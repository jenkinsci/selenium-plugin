package hudson.plugins.selenium.configuration.browser;

import hudson.Extension;
import hudson.model.Descriptor;

import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;
import org.openqa.selenium.remote.BrowserType;

public class IEBrowser extends Browser {
	
	transient private static final String BROWSER_NAME = "internet explorer";
	
	@DataBoundConstructor
	public IEBrowser(int maxInstances, String version, String binary) {
		super(maxInstances, version);
	}
	
	public String getBrowserName() {
		return BROWSER_NAME;
	}
	
    @Extension
    public static class DescriptorImpl extends BrowserDescriptor {

		@Override
		public String getDisplayName() {
			return "Internet Explorer";
		}

    }
}
