package hudson.plugins.selenium.configuration.browser.selenium;

import hudson.Extension;
import hudson.model.Computer;
import hudson.plugins.selenium.configuration.browser.SeleniumBrowserServerUtils;
import hudson.plugins.selenium.process.SeleniumRunOptions;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;

public class IEBrowser extends SeleniumBrowser {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1128527306600780412L;

    private String server_binary;

    transient private static final String PARAM_BINARY_PATH = "webdriver.ie.driver";

    @DataBoundConstructor
    public IEBrowser(int maxInstances, String version, String server_binary) {
        super(maxInstances, version, "*iexplore");
        this.server_binary = server_binary;
    }

    @Exported
    public String getBinary() {
        return server_binary;
    }

    @Override
    public void initOptions(Computer c, SeleniumRunOptions opt) {
        String server_path = SeleniumBrowserServerUtils.uploadIEDriverIfNecessary(c, getBinary());
        if (server_path != null) {
            opt.getJVMArguments().put(PARAM_BINARY_PATH, server_path);
        }
        opt.addOptionIfSet("-browser", StringUtils.join(initBrowserOptions(c, opt), ","));
    }

    @Extension
    public static class DescriptorImpl extends SeleniumBrowserDescriptor {

        @Override
        public String getDisplayName() {
            return "Internet Explorer";
        }

    }
}
