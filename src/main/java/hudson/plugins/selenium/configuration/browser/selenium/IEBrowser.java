package hudson.plugins.selenium.configuration.browser.selenium;

import hudson.Extension;
import hudson.model.Computer;
import hudson.plugins.selenium.configuration.browser.IeDriverServerUtils;
import hudson.plugins.selenium.process.SeleniumRunOptions;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;

public class IEBrowser extends SeleniumBrowser {

    /**
	 * 
	 */
    private static final long serialVersionUID = 2L;

    private String driverBinaryPath;

    private transient boolean forbid64bitDriver;

    private transient final String ieDriverProperty = "webdriver.ie.driver";

    @DataBoundConstructor
    public IEBrowser(int maxInstances, String version, String driverBinaryPath, boolean forbid64bitDriver) {
        super(maxInstances, version, "*iexplore");
        this.driverBinaryPath = driverBinaryPath;
        this.forbid64bitDriver = forbid64bitDriver;
    }

    @Exported
    public String getDriverBinaryPath() {
        return driverBinaryPath;
    }

    @Exported
    public boolean getForbid64bitDriver() {
        return forbid64bitDriver;
    }

    @Override
    public void initOptions(Computer c, SeleniumRunOptions opt) {
        String serverPath = IeDriverServerUtils.uploadIEDriverIfNecessary(c, getDriverBinaryPath(), getForbid64bitDriver());
        if (serverPath != null) {
            opt.getJVMArguments().put(ieDriverProperty, serverPath);
        }
        opt.addOptionIfSet("-browser", StringUtils.join(initBrowserOptions(c, opt), ","));
    }

    @Extension
    public static class DescriptorImpl extends SeleniumBrowserDescriptor {

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

            this.driverBinaryPath = server_binary;
        }
        return this;
    }
}
