package hudson.plugins.selenium.configuration.browser.webdriver;

import hudson.Extension;
import hudson.util.FormValidation;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class ChromeBrowser extends ServerRequiredWebDriverBrowser {

    /**
	 * 
	 */
    private static final long serialVersionUID = 8505665387429684157L;

    /**
     * System property to specify the chrome binary location. Could be done through a tool installer and probably moved into the chromedriver plugin.
     */
    transient final protected String PARAM_BINARY_PATH = "webdriver.chrome.driver";

    @DataBoundConstructor
    public ChromeBrowser(int maxInstances, String version, String serverBinary) {
        super(maxInstances, version, "chrome", serverBinary);
    }

    @Override
    public Map<String, String> getJVMArgs() {
        Map<String, String> args = new HashMap<String, String>();
        combine(args, PARAM_BINARY_PATH, getServer_binary());
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

        public FormValidation doCheckServer_binary(@QueryParameter String value) {
            if (StringUtils.isBlank(value)) {
                return FormValidation
                        .warning("Must not be empty unless it is already defined from a previous chrome browser definition or already defined in the path");
            }
            return FormValidation.ok();
        }

    }
}
