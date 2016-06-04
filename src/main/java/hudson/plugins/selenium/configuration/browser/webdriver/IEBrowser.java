package hudson.plugins.selenium.configuration.browser.webdriver;

import hudson.Extension;
import hudson.model.Computer;
import hudson.plugins.selenium.configuration.browser.IeDriverServerUtils;
import hudson.plugins.selenium.process.SeleniumRunOptions;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.HashMap;
import java.util.Map;

public class IEBrowser extends DriverRequiredWebDriverBrowser {

    /**
	 * 
	 */
    private static final long serialVersionUID = -241845413478474187L;

    private String ieDriverProperty = "webdriver.ie.driver";

    @DataBoundConstructor
    public IEBrowser(int maxInstances, String version, String driverBinaryPath) {
        super(maxInstances, version, "internet explorer", driverBinaryPath);
    }

    @Override
    public Map<String, String> getJVMArgs() {
        Map<String, String> args = new HashMap<String, String>();

        combine(args, ieDriverProperty, getDriverBinaryPath());
        return args;
    }

    @Override
    public void initOptions(Computer c, SeleniumRunOptions opt) {
        String driverPath = IeDriverServerUtils.uploadIEDriverIfNecessary(c, getDriverBinaryPath());
        if (driverPath != null) {
            opt.getJVMArguments().put(ieDriverProperty, driverPath);
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
