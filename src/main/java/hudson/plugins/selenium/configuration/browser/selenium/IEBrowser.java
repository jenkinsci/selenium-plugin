package hudson.plugins.selenium.configuration.browser.selenium;

import hudson.Extension;

import org.kohsuke.stapler.DataBoundConstructor;

public class IEBrowser extends SeleniumBrowser {
	
	@DataBoundConstructor
	public IEBrowser(int maxInstances, String version, String binary) {
		super(maxInstances, version, "*iexplore");
	}
	
    @Extension
    public static class DescriptorImpl extends SeleniumBrowserDescriptor {

		@Override
		public String getDisplayName() {
			return "Internet Explorer";
		}

    }
}
