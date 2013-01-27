package hudson.plugins.selenium.configuration.browser.webdriver;

import hudson.Extension;

import org.kohsuke.stapler.DataBoundConstructor;


public class HTMLUnitBrowser extends WebDriverBrowser {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7089526214694718987L;


	@DataBoundConstructor
	public HTMLUnitBrowser(int maxInstances) {
		super(maxInstances, null, "htmlunit");
	}
	
	
    @Extension
    public static class DescriptorImpl extends WebDriverBrowserDescriptor {
    	
		@Override
		public String getDisplayName() {
			return "HTMLUnit";
		}
	
    }
}
