package hudson.plugins.selenium.configuration;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.plugins.selenium.SeleniumRunOptions;
import hudson.plugins.selenium.configuration.browser.Browser;
import hudson.plugins.selenium.configuration.browser.BrowserDescriptor;
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

public class CustomConfiguration extends Configuration {

	private int port = 4444;
    private boolean rcBrowserSideLog;
    private boolean rcDebug;
    private boolean rcTrustAllSSLCerts;
    private boolean rcBrowserSessionReuse;
    private Integer timeout = -1;
    private String rcLog;
    private List<? extends Browser> browsers = new ArrayList<Browser>();
    private String display;

    @DataBoundConstructor
    public CustomConfiguration(int port, 
    							boolean rcBrowserSideLog, 
    							boolean rcDebug, 
    							boolean rcTrustAllSSLCerts, 
    							boolean rcBrowserSessionReuse,
    							Integer timeout,
    							String rcLog, 
    							List<? extends Browser> browsers,
    							String display) {
    	this.port = port;
    	this.rcBrowserSideLog = rcBrowserSideLog;
    	this.rcDebug = rcDebug;
    	this.rcTrustAllSSLCerts = rcTrustAllSSLCerts;
    	this.rcBrowserSessionReuse = rcBrowserSessionReuse;
    	this.rcLog = rcLog;
    	this.timeout = timeout;
    	this.browsers = browsers;
    	this.display = display;
    	
    }
    
    @Exported
    public String getRcLog(){
        return rcLog;
    }

    @Exported
    public boolean getRcBrowserSideLog(){
        return rcBrowserSideLog;
    }

    @Exported
    public boolean getRcDebug(){
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
    public List<? extends Browser> getBrowsers() {
    	return browsers;
    }
    
	public DescriptorExtensionList<Browser, BrowserDescriptor> getBrowserTypes() {
		return Browser.all();
	}
	
	@Exported
	public String getDisplay() {
		return display;
	}
	
	@Extension
	public static class DescriptorImpl extends ConfigurationDescriptor {

		@Override
		public String getDisplayName() {
			return "Custom configuration";
		}
		
		@Override
		public CustomConfiguration newInstance(StaplerRequest req, JSONObject json) {
			
			//String rcLog = json.getString("rcLog");
			
			return req.bindJSON(CustomConfiguration.class, json);
		}
		
		public static List<Descriptor<Browser>> getBrowserTypes() {
			List<Descriptor<Browser>> lst = new ArrayList<Descriptor<Browser>>();
			for (BrowserDescriptor b : Browser.all()) {
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
        opt.addOptionIfSet("-log", getRcLog());
        if (getRcBrowserSideLog()){
        	opt.addOption("-browserSideLog");
        }
        if (getRcDebug()){
        	opt.addOption("-debug");
        }
        if (getRcTrustAllSSLCerts()){
        	opt.addOption("-trustAllSSLCertificates");
        }
        if (getRcBrowserSessionReuse()) {
        	opt.addOption("-browserSessionReuse");
        }
        if (getTimeout() != null && getTimeout() > -1) {
	        opt.addOption("-timeout");
	        opt.addOption(getTimeout().toString());
        }
        //addIfHasText(args, "-firefoxProfileTemplate", getRcFirefoxProfileTemplate());
        for (Browser b : browsers) {
        	b.initOptions(c, opt);
        }

        opt.setEnvVar("DISPLAY", display);
        
		return opt;
	}
	
}
