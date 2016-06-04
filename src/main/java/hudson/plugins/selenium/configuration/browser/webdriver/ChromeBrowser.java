package hudson.plugins.selenium.configuration.browser.webdriver;

import hudson.Extension;
import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.util.HashMap;
import java.util.Map;

public class ChromeBrowser extends DriverRequiredWebDriverBrowser {

    /**
	 * 
	 */
    private static final long serialVersionUID = 8505665387429684157L;

    /**
     * System property to specify the chrome binary location. Could be done through a tool installer and probably moved into the chromedriver plugin.
     */
    private String chromeDriverProperty = "webdriver.chrome.driver";

    @DataBoundConstructor
    public ChromeBrowser(int maxInstances, String version, String driverBinaryPath) {
        super(maxInstances, version, "chrome", driverBinaryPath);
    }

    @Override
    public Map<String, String> getJVMArgs() {
        Map<String, String> args = new HashMap<String, String>();
        combine(args, chromeDriverProperty, getDriverBinaryPath());
        return args;
    }

    @Extension
    public static class DescriptorImpl extends WebDriverBrowserDescriptor {

        public int getMaxInstances() {
            return 5;
        }

        @Override
        public String getDisplayName() {
            return "Chrome";
        }

        public FormValidation doCheckDriverBinaryPath(@QueryParameter String value) {
            if (StringUtils.isBlank(value)) {
                return FormValidation
                        .warning("Must not be empty unless it is already defined from a previous chrome browser definition or already defined in the path");
            }
            return FormValidation.ok();
        }
    }
}
