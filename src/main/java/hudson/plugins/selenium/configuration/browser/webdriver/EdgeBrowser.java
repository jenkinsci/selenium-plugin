package hudson.plugins.selenium.configuration.browser.webdriver;

import hudson.Extension;
import hudson.model.Computer;
import hudson.plugins.selenium.configuration.browser.EdgeDriverServerUtils;
import hudson.plugins.selenium.process.SeleniumRunOptions;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.HashMap;
import java.util.Map;

public class EdgeBrowser extends DriverRequiredWebDriverBrowser {

    /**
	 *
	 */
    private static final long serialVersionUID = 4134783568959523121L;

    private transient final String edgeDriverProperty = "webdriver.edge.driver";

    @DataBoundConstructor
    public EdgeBrowser(int maxInstances, String version, String driverBinaryPath) {
        super(maxInstances, version, "MicrosoftEdge", driverBinaryPath);
    }

    @Override
    public Map<String, String> getJVMArgs() {
        Map<String, String> args = new HashMap<String, String>();

        combine(args, edgeDriverProperty, getDriverBinaryPath());
        return args;
    }

    @Override
    public void initOptions(Computer c, SeleniumRunOptions opt) {
        String driverPath = EdgeDriverServerUtils.uploadEdgeDriverIfNecessary(c, getDriverBinaryPath());
        if (driverPath != null) {
            opt.getJVMArguments().put(edgeDriverProperty, driverPath);
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
            return "Microsoft Edge";
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
}
