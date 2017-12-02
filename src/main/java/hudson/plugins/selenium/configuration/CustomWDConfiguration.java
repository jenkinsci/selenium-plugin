package hudson.plugins.selenium.configuration;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.plugins.selenium.callables.RetrieveAvailablePort;
import hudson.plugins.selenium.configuration.browser.webdriver.ChromeBrowser;
import hudson.plugins.selenium.configuration.browser.webdriver.FirefoxBrowser;
import hudson.plugins.selenium.configuration.browser.webdriver.IEBrowser;
import hudson.plugins.selenium.configuration.browser.webdriver.WebDriverBrowser;
import hudson.plugins.selenium.configuration.browser.webdriver.WebDriverBrowser.WebDriverBrowserDescriptor;
import hudson.plugins.selenium.process.SeleniumRunOptions;
import hudson.util.FormValidation;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.export.Exported;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomWDConfiguration extends SeleniumNodeConfiguration {

    private static final Logger LOGGER = Logger.getLogger(CustomWDConfiguration.class.getName());

    /**
	 * 
	 */
	private static final long serialVersionUID = -1357952133882786829L;

	private int port = 4444;
    private Integer maxSession = 5;
    private Integer timeout = -1;

    private List<WebDriverBrowser> browsers = new ArrayList<WebDriverBrowser>();

    private CustomWDConfiguration() {
        super(null);
        browsers.add(new IEBrowser(1, "", "", false));
        browsers.add(new FirefoxBrowser(5, "", ""));
        browsers.add(new ChromeBrowser(5, "", ""));
    }

    @DataBoundConstructor
    public CustomWDConfiguration(int port, Integer timeout, List<WebDriverBrowser> browsers, String display, Integer maxSession) {
        super(display);
        this.port = port;
        this.timeout = timeout;
        this.browsers = browsers;
        this.maxSession = maxSession;
    }

    @Exported
    public int getPort() {
        return port;
    }

    @Exported
    public Integer getTimeout() {
        return timeout;
    }

    @Exported
    public Integer getMaxSession() { return maxSession; }

    @Exported
    public List<WebDriverBrowser> getBrowsers() {
        return browsers;
    }

    @Extension
    public static class DescriptorImpl extends ConfigurationDescriptor {

        @Override
        public String getDisplayName() {
            return "Custom web driver node configuration";
        }

        public CustomWDConfiguration getDefault() {
            return new CustomWDConfiguration();
        }

        public static List<Descriptor<WebDriverBrowser>> getBrowserTypes() {
            List<Descriptor<WebDriverBrowser>> lst = new ArrayList<Descriptor<WebDriverBrowser>>();
            for (WebDriverBrowserDescriptor b : WebDriverBrowser.all()) {
                lst.add(b);
            }
            return lst;
        }

        public FormValidation doCheckTimeout(@QueryParameter String value) {
            try {
                Integer i = Integer.parseInt(value);
                if (i >= -1) {
                    return FormValidation.ok();
                }
            } catch (NumberFormatException nfe) {

            }
            return FormValidation.error("Must be an integer greater than or equal to -1.");
        }

        public FormValidation doCheckMaxSession(@QueryParameter String value) {
            try {
                Integer i = Integer.parseInt(value);
                if (i >= 1) {
                    return FormValidation.ok();
                }
            } catch (NumberFormatException nfe) {

            }
            return FormValidation.error("Must be an integer greater than or equal to 1.");
        }
    }

    @Override
    public SeleniumRunOptions initOptions(Computer c) {
        SeleniumRunOptions opt = super.initOptions(c);
        try {
            opt.addOptionIfSet("-port", c.getChannel().call(new RetrieveAvailablePort(getPort())));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "an error occured, not adding the port option", e);
        }

        if (getTimeout() != null && getTimeout() > -1) {
            opt.addOption("-timeout");
            opt.addOption(getTimeout().toString());
        }

        if (getMaxSession() != null && getMaxSession() > 0) {
            opt.addOption("-maxSession");
            opt.addOption(getMaxSession().toString());
        }

        for (WebDriverBrowser b : browsers) {
            b.initOptions(c, opt);
        }

        return opt;
    }

    public String getIcon() {
        return "/plugin/selenium/24x24/internet-web-browser.png";
    }

}
