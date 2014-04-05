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

        public int getMaxInstances() {
            return 1;
        }

        @Override
        public String getDisplayName() {
            return "HTMLUnit";
        }

    }
}
