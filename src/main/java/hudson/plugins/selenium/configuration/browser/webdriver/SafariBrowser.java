package hudson.plugins.selenium.configuration.browser.webdriver;

import hudson.Extension;

import org.kohsuke.stapler.DataBoundConstructor;

public class SafariBrowser extends WebDriverBrowser {

	@DataBoundConstructor
	public SafariBrowser(int instances, String version) {
		super(instances, version, "safari");
	}
	
	@Extension
	public static class DescriptorImpl extends WebDriverBrowserDescriptor {
		
		public String getMaxInstances() {
			return "5";
		}
		
		public String getDisplayName() {
			return "Safari";
		}

	}

}
