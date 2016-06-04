package hudson.plugins.selenium.configuration.browser.selenium;

import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;

import java.util.HashMap;
import java.util.Map;

public class OperaBrowser extends SeleniumBrowser {

    /**
	 * 
	 */
    private static final long serialVersionUID = -7520649475709638350L;

    private String paramBinaryPath = "opera.binary";

    private String binaryPath;

    @DataBoundConstructor
    public OperaBrowser(int maxInstances, String version, String binaryPath) {
        super(maxInstances, version, "*opera");
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
    public static class DescriptorImpl extends SeleniumBrowserDescriptor {

        public int getMaxInstances() {
            return 1;
        }

        @Override
        public String getDisplayName() {
            return "Opera";
        }

    }
}
