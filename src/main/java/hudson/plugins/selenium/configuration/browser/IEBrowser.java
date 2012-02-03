package hudson.plugins.selenium.configuration.browser;

import hudson.Extension;
import hudson.model.Descriptor;

import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;
import org.openqa.selenium.remote.BrowserType;

public class IEBrowser extends Browser {
	
	@DataBoundConstructor
	public IEBrowser(int maxInstances, String version, String binary) {
		super(maxInstances, version);
	}
	
	public String getBrowserName() {
		return BrowserType.IEXPLORE;
	}
	
    @Extension
    public static class DescriptorImpl extends BrowserDescriptor {

		@Override
		public String getDisplayName() {
			return "Internet Explorer";
		}

    }
}
