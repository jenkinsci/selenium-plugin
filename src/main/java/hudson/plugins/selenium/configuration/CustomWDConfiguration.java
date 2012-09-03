package hudson.plugins.selenium.configuration;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.plugins.selenium.SeleniumRunOptions;
import hudson.plugins.selenium.callables.RetrieveAvailablePort;
import hudson.plugins.selenium.configuration.browser.AbstractSeleniumBrowser;
import hudson.plugins.selenium.configuration.browser.webdriver.ChromeBrowser;
import hudson.plugins.selenium.configuration.browser.webdriver.FirefoxBrowser;
import hudson.plugins.selenium.configuration.browser.webdriver.IEBrowser;
import hudson.plugins.selenium.configuration.browser.webdriver.WebDriverBrowser;
import hudson.plugins.selenium.configuration.browser.webdriver.WebDriverBrowser.WebDriverBrowserDescriptor;
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

public class CustomWDConfiguration extends SeleniumNodeConfiguration {

	private int port = 4444;
    private Integer timeout = -1;
    private List<WebDriverBrowser> browsers = new ArrayList<WebDriverBrowser>();
    private String display;

    private CustomWDConfiguration() {
    	browsers.add(new IEBrowser(1, "", ""));
		browsers.add(new FirefoxBrowser(5, "", ""));
		browsers.add(new ChromeBrowser(5, "", ""));
    }
    
    @DataBoundConstructor
    public CustomWDConfiguration(int port, 
    							Integer timeout,
    							List<WebDriverBrowser> browsers,
    							String display) {
    	this.port = port;
    	this.timeout = timeout;
    	this.browsers = browsers;
    	this.display = display;
    	
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
    public List<? extends AbstractSeleniumBrowser> getBrowsers() {
    	return browsers;
    }
    
//	public DescriptorExtensionList<AbstractSeleniumBrowser, BrowserDescriptor> getBrowserTypes() {
//		return AbstractSeleniumBrowser.all();
//	}
	
	@Exported
	public String getDisplay() {
		return display;
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
		
		@Override
		public CustomWDConfiguration newInstance(StaplerRequest req, JSONObject json) {
			
			//String rcLog = json.getString("rcLog");
			
			return req.bindJSON(CustomWDConfiguration.class, json);
		}
		
		public static List<Descriptor<WebDriverBrowser>> getBrowserTypes() {
			List<Descriptor<WebDriverBrowser>> lst = new ArrayList<Descriptor<WebDriverBrowser>>();
			for (WebDriverBrowserDescriptor b : WebDriverBrowser.all()) {
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
		SeleniumRunOptions opt = new SeleniumRunOptions();
        try {
        
        	int p = c.getChannel().call(new RetrieveAvailablePort(getPort()));
        	if (p != -1) {
        		opt.addOptionIfSet("-port", getPort());
        	}
        } catch (Exception e) {
        	// an error occured, not adding the port option
            //e.printStackTrace();
        }
        
        if (getTimeout() != null && getTimeout() > -1) {
	        opt.addOption("-timeout");
	        opt.addOption(getTimeout().toString());
        }
        //addIfHasText(args, "-firefoxProfileTemplate", getRcFirefoxProfileTemplate());
        for (AbstractSeleniumBrowser b : browsers) {
        	b.initOptions(c, opt);
        }
        
        if (display != null && !display.equals("")) {
        	opt.setEnvVar("DISPLAY", display);
        }        
		
        return opt;
	}

	public String getIcon() {
		return "/plugin/selenium/24x24/internet-web-browser.png";
	}

	
}
