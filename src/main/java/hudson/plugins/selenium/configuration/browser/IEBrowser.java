package hudson.plugins.selenium.configuration.browser;

import hudson.Extension;

import org.kohsuke.stapler.DataBoundConstructor;

public class IEBrowser extends Browser {
	
	transient private static final String WD_BROWSER_NAME = "internet explorer";
	
	transient private static final String RC_BROWSER_NAME = "*iexplore";
	
	@DataBoundConstructor
	public IEBrowser(int maxInstances, String version, String binary) {
		super(maxInstances, version);
	}
	
	@Override
	public String getBrowserName() {
		return WD_BROWSER_NAME;
	}
	
	@Override
	public String getRCBrowserName() {
		return RC_BROWSER_NAME;
	}	

    @Extension
    public static class DescriptorImpl extends BrowserDescriptor {

		@Override
		public String getDisplayName() {
			return "Internet Explorer";
		}

    }
}
