package hudson.plugins.selenium.configuration.browser.webdriver;

import hudson.Extension;

import org.kohsuke.stapler.DataBoundConstructor;

public class SafariBrowser extends WebDriverBrowser {

    /**
	 * 
	 */
    private static final long serialVersionUID = 5425432330259522911L;

    @DataBoundConstructor
    public SafariBrowser(int maxInstances, String version) {
        super(maxInstances, version, "safari");
    }

    @Extension
    public static class DescriptorImpl extends WebDriverBrowserDescriptor {

        public int getMaxInstances() {
            return 1;
        }

        public String getDisplayName() {
            return "Safari";
        }

    }

}
