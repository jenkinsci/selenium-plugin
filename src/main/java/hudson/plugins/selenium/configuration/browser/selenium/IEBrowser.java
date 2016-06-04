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
    private static final long serialVersionUID = 1128527306600780412L;

    private String driverBinaryPath;

    private String ieDriverProperty = "webdriver.ie.driver";

    @DataBoundConstructor
    public IEBrowser(int maxInstances, String version, String driverBinaryPath) {
        super(maxInstances, version, "*iexplore");
        this.driverBinaryPath = driverBinaryPath;
    }

    @Exported
    public String getDriverBinaryPath() {
        return driverBinaryPath;
    }

    @Override
    public void initOptions(Computer c, SeleniumRunOptions opt) {
        String serverPath = IeDriverServerUtils.uploadIEDriverIfNecessary(c, getDriverBinaryPath());
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
}
