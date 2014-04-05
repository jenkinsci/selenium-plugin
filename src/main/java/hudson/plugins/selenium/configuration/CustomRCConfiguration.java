package hudson.plugins.selenium.configuration;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.plugins.selenium.callables.RetrieveAvailablePort;
import hudson.plugins.selenium.configuration.browser.selenium.SeleniumBrowser;
import hudson.plugins.selenium.configuration.browser.selenium.SeleniumBrowser.SeleniumBrowserDescriptor;
import hudson.plugins.selenium.configuration.browser.selenium.ChromeBrowser;
import hudson.plugins.selenium.configuration.browser.selenium.FirefoxBrowser;
import hudson.plugins.selenium.configuration.browser.selenium.IEBrowser;
import hudson.plugins.selenium.process.SeleniumRunOptions;
import hudson.util.FormValidation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;

public class CustomRCConfiguration extends SeleniumNodeConfiguration {

    private int port = 4444;
    private boolean rcBrowserSideLog;
    private boolean rcDebug;
    private boolean rcTrustAllSSLCerts;
    private boolean rcBrowserSessionReuse;
    private Integer timeout = -1;
    private String rcLog;
    private List<SeleniumBrowser> browsers = new ArrayList<SeleniumBrowser>();

    private CustomRCConfiguration() {
        super(null);
        browsers.add(new IEBrowser(1, "", ""));
        browsers.add(new FirefoxBrowser(5, "", ""));
        browsers.add(new ChromeBrowser(5, "", ""));
    }

    @DataBoundConstructor
    public CustomRCConfiguration(int port, boolean rcBrowserSideLog, boolean rcDebug, boolean rcTrustAllSSLCerts, boolean rcBrowserSessionReuse,
            Integer timeout, String rcLog, List<SeleniumBrowser> browsers, String display) {
        super(display);
        this.port = port;
        this.rcBrowserSideLog = rcBrowserSideLog;
        this.rcDebug = rcDebug;
        this.rcTrustAllSSLCerts = rcTrustAllSSLCerts;
        this.rcBrowserSessionReuse = rcBrowserSessionReuse;
        this.rcLog = rcLog;
        this.timeout = timeout;
        this.browsers = browsers;

    }

    @Exported
    public String getRcLog() {
        return rcLog;
    }

    @Exported
    public boolean getRcBrowserSideLog() {
        return rcBrowserSideLog;
    }

    @Exported
    public boolean getRcDebug() {
        return rcDebug;
    }

    @Exported
    public boolean getRcTrustAllSSLCerts() {
        return rcTrustAllSSLCerts;
    }

    @Exported
    public boolean getRcBrowserSessionReuse() {
        return rcBrowserSessionReuse;
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
    public List<SeleniumBrowser> getBrowsers() {
        return browsers;
    }

    @Extension
    public static class DescriptorImpl extends ConfigurationDescriptor {

        @Override
        public String getDisplayName() {
            return "Custom RC node configuration";
        }

        public CustomRCConfiguration getDefault() {
            return new CustomRCConfiguration();
        }

        @Override
        public CustomRCConfiguration newInstance(StaplerRequest req, JSONObject json) {
            return req.bindJSON(CustomRCConfiguration.class, json);
        }

        public static List<Descriptor<SeleniumBrowser>> getBrowserTypes() {
            List<Descriptor<SeleniumBrowser>> lst = new ArrayList<Descriptor<SeleniumBrowser>>();
            for (SeleniumBrowserDescriptor b : SeleniumBrowser.all()) {
                lst.add(b);
            }
            return lst;
        }

        public FormValidation doCheckTimeout(@QueryParameter String value) throws IOException, ServletException {
            try {
                Integer i = Integer.parseInt(value);
                if (i >= -1) {
                    return FormValidation.ok();
                }
            } catch (NumberFormatException nfe) {

            }
            return FormValidation.error("Must be an integer greater than or equal to -1.");
        }

    }

    @Override
    public SeleniumRunOptions initOptions(Computer c) {
        SeleniumRunOptions opt = super.initOptions(c);

        opt.addOptionIfSet("-log", getRcLog());

        try {
            opt.addOptionIfSet("-port", c.getChannel().call(new RetrieveAvailablePort(getPort())));
        } catch (Exception e) {
            // an error occured, not adding the port option
        }

        if (getRcBrowserSideLog()) {
            opt.addOption("-browserSideLog");
        }
        if (getRcDebug()) {
            opt.addOption("-debug");
        }
        if (getRcTrustAllSSLCerts()) {
            opt.addOption("-trustAllSSLCertificates");
        }
        if (getRcBrowserSessionReuse()) {
            opt.addOption("-browserSessionReuse");
        }
        if (getTimeout() != null && getTimeout() > -1) {
            opt.addOption("-timeout");
            opt.addOption(getTimeout().toString());
        }

        for (SeleniumBrowser b : browsers) {
            b.initOptions(c, opt);
        }

        return opt;
    }

    public String getIcon() {
        return "/plugin/selenium/24x24/internet-web-browser.png";
    }

}
