package hudson.plugins.selenium.configuration.browser.selenium;

import hudson.Extension;
import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.export.Exported;

import java.util.HashMap;
import java.util.Map;

public class ChromeBrowser extends SeleniumBrowser {

    /**
	 * 
	 */
    private static final long serialVersionUID = -7028484889764200348L;

    private String chromeDriverProperty = "webdriver.chrome.driver";

    private String driverBinaryPath;

    @DataBoundConstructor
    public ChromeBrowser(int maxInstances, String version, String driverBinaryPath) {
        super(maxInstances, version, "*googlechrome");
        this.driverBinaryPath = driverBinaryPath;
    }

    @Exported
    public String getDriverBinaryPath() {
        return driverBinaryPath;
    }

    @Override
    public Map<String, String> getJVMArgs() {
        Map<String, String> args = new HashMap<String, String>();
        combine(args, chromeDriverProperty, getDriverBinaryPath());
        return args;
    }

    @Extension
    public static class DescriptorImpl extends SeleniumBrowserDescriptor {

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
