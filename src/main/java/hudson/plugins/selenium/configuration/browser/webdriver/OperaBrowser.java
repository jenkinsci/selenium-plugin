package hudson.plugins.selenium.configuration.browser.webdriver;

import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;

import java.util.HashMap;
import java.util.Map;

public class OperaBrowser extends WebDriverBrowser {

    /**
	 * 
	 */
    private static final long serialVersionUID = 2L;

    private transient final String paramBinaryPath = "opera.binary";

    private String binaryPath;

    @DataBoundConstructor
    public OperaBrowser(int maxInstances, String version, String binaryPath) {
        super(maxInstances, version, "opera");
        this.binaryPath = binaryPath;
    }

    @Exported
    public String getBinaryPath() {
        return binaryPath;
    }

    @Override
    public Map<String, String> getJVMArgs() {
        Map<String, String> args = new HashMap<String, String>();
        combine(args, paramBinaryPath, getBinaryPath());
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

    // Backwards compatibility since 2.4.1
    @Deprecated
    transient private String browser_binary;
    @Deprecated
    public String getBrowser_binary() {
        return browser_binary;
    }

    public Object readResolve() {

        if (browser_binary != null) {

            this.binaryPath = browser_binary;
        }
        return this;
    }
}
