package hudson.plugins.selenium.configuration.browser.webdriver;

import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.HashMap;
import java.util.Map;

public class OperaBrowser extends WebDriverBrowser {

    /**
	 * 
	 */
    private static final long serialVersionUID = -5094330146488965759L;

    transient final protected String paramBinaryPath = "opera.binary";

    private String browserBinary;

    @DataBoundConstructor
    public OperaBrowser(int maxInstances, String version, String browser_binary) {
        super(maxInstances, version, "operablink");
        this.browserBinary = browser_binary;
    }

    @Override
    public Map<String, String> getJVMArgs() {
        Map<String, String> args = new HashMap<String, String>();
        combine(args, paramBinaryPath, browserBinary);
        return args;
    }

    @Extension
    public static class DescriptorImpl extends WebDriverBrowserDescriptor {

        public int getMaxInstances() {
            return 1;
        }

        @Override
        public String getDisplayName() {
            return "Opera";
        }

    }
}
