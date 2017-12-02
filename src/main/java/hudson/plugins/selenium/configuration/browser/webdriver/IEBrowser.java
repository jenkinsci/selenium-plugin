package hudson.plugins.selenium.configuration.browser.webdriver;

import hudson.Extension;
import hudson.model.Computer;
import hudson.plugins.selenium.configuration.browser.IeDriverServerUtils;
import hudson.plugins.selenium.process.SeleniumRunOptions;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;

import java.util.HashMap;
import java.util.Map;

public class IEBrowser extends DriverRequiredWebDriverBrowser {

    /**
	 * 
	 */
    private static final long serialVersionUID = -241845413478474187L;

    private transient final String ieDriverProperty = "webdriver.ie.driver";

    private transient boolean forbid64bitDriver;

    @DataBoundConstructor
    public IEBrowser(int maxInstances, String version, String driverBinaryPath, boolean forbid64bitDriver) {
        super(maxInstances, version, "internet explorer", driverBinaryPath);
        this.forbid64bitDriver = forbid64bitDriver;
    }

    @Override
    public Map<String, String> getJVMArgs() {
        Map<String, String> args = new HashMap<String, String>();

        combine(args, ieDriverProperty, getDriverBinaryPath());
        return args;
    }

    @Override
    public void initOptions(Computer c, SeleniumRunOptions opt) {
        String driverPath = IeDriverServerUtils.uploadIEDriverIfNecessary(c, getDriverBinaryPath(), getForbid64bitDriver());
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

    // Backwards compatibility since 2.4.1
    @Deprecated
    transient private String server_binary;

    @Deprecated
    public String getServer_binary() {

        return server_binary;
    }

    public Object readResolve() {

        if (server_binary != null) {

            setDriverBinaryPath(server_binary);
        }
        return this;
    }

    @Exported
    public boolean getForbid64bitDriver() {
        return forbid64bitDriver;
    }
}
