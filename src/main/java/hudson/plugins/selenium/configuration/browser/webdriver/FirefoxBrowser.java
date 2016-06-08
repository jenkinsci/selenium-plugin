package hudson.plugins.selenium.configuration.browser.webdriver;

import hudson.Extension;
import hudson.model.Computer;
import hudson.plugins.selenium.process.SeleniumRunOptions;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;

import java.util.List;

public class FirefoxBrowser extends WebDriverBrowser {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1451746845341944745L;

    private transient final String paramBinaryPath = "firefox_binary";

    private String binaryPath;

    @DataBoundConstructor
    public FirefoxBrowser(int maxInstances, String version, String binaryPath) {
        super(maxInstances, version, "firefox");
        this.binaryPath = binaryPath;
    }

    @Exported
    public String getBinaryPath() {
        return binaryPath;
    }

    @Override
    public List<String> initBrowserOptions(Computer c, SeleniumRunOptions options) {
        List<String> args = super.initBrowserOptions(c, options);
        combine(args, paramBinaryPath, getBinaryPath());
        return args;
    }

    @Extension
    public static class DescriptorImpl extends WebDriverBrowserDescriptor {

        public int getMaxInstances() {
            return 5;
        }

        @Override
        public String getDisplayName() {
            return "Firefox";
        }

    }

    // Backwards compatibility since 2.4.1
    @Deprecated
    transient private String binary_path;
    @Deprecated
    public String getBinary_path() { return binary_path; }

    public Object readResolve() {

        if (binary_path != null) {

            this.binaryPath = binary_path;
        }

        return this;
    }
}
