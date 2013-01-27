package hudson.plugins.selenium.configuration.browser.selenium;

import hudson.Extension;

import org.kohsuke.stapler.DataBoundConstructor;

public class IEBrowser extends SeleniumBrowser {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1128527306600780412L;

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
