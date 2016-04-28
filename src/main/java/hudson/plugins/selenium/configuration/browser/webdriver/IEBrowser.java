package hudson.plugins.selenium.configuration.browser.webdriver;

import hudson.Extension;
import hudson.model.Computer;
import hudson.plugins.selenium.configuration.browser.SeleniumBrowserServerUtils;
import hudson.plugins.selenium.process.SeleniumRunOptions;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.HashMap;
import java.util.Map;

public class IEBrowser extends ServerRequiredWebDriverBrowser {

    /**
	 * 
	 */
    private static final long serialVersionUID = -241845413478474187L;

    transient private static final String PARAM_BINARY_PATH = "webdriver.ie.driver";

    @DataBoundConstructor
    public IEBrowser(int maxInstances, String version, String serverBinary) {
        super(maxInstances, version, "internet explorer", serverBinary);
    }

    @Override
    public Map<String, String> getJVMArgs() {
        Map<String, String> args = new HashMap<String, String>();

        combine(args, PARAM_BINARY_PATH, getServer_binary());
        return args;
    }

    @Override
    public void initOptions(Computer c, SeleniumRunOptions opt) {
        String serverPath = SeleniumBrowserServerUtils.uploadIEDriverIfNecessary(c, getServer_binary());
        if (serverPath != null) {
            opt.getJVMArguments().put(PARAM_BINARY_PATH, serverPath);
        }
        opt.addOptionIfSet("-browser", StringUtils.join(initBrowserOptions(c, opt), ","));
    }

    @Extension
    public static class DescriptorImpl extends WebDriverBrowserDescriptor {

        public int getMaxInstances() {
            return 1;
        }

        @Override
        public String getDisplayName() {
            return "Internet Explorer";
        }

    }
}
