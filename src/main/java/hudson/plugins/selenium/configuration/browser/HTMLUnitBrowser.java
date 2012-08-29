package hudson.plugins.selenium.configuration.browser;

import hudson.Extension;

import org.kohsuke.stapler.DataBoundConstructor;


public class HTMLUnitBrowser extends Browser {
	
	transient private static final String WD_BROWSER_NAME = "htmlunit";

	@DataBoundConstructor
	public HTMLUnitBrowser(int maxInstances) {
		super(maxInstances, null);
	}
	
	
	@Override
	public String getBrowserName() {
		return WD_BROWSER_NAME;
	}
	
	@Override
	public String getRCBrowserName() {
		return null;
	}	
	
    @Extension
    public static class DescriptorImpl extends BrowserDescriptor {
    	
		@Override
		public String getDisplayName() {
			return "HTMLUnit";
		}
	
    }
}
