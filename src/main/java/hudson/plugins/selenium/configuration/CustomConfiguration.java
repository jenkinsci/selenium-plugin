package hudson.plugins.selenium.configuration;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.plugins.selenium.configuration.browser.Browser;
import hudson.plugins.selenium.configuration.browser.BrowserDescriptor;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;
import org.springframework.util.StringUtils;

public class CustomConfiguration extends Configuration {

    private boolean rcBrowserSideLog;
    private boolean rcDebug;
    private boolean rcTrustAllSSLCerts;
    private boolean rcBrowserSessionReuse;
    private String rcFirefoxProfileTemplate;
    private String rcLog;
    private List<BrowserDescriptor> browsers = new ArrayList<BrowserDescriptor>();

    @DataBoundConstructor
    public CustomConfiguration() {
    	
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
    public String getRcFirefoxProfileTemplate(){
        return rcFirefoxProfileTemplate;
    }

	@Override
	public List<String> getLaunchingArguments() {
        List<String> args = new ArrayList<String>();
        addIfHasText(args, "-log", getRcLog());
        if (getRcBrowserSideLog()){
        	args.add("-browserSideLog");
        }
        if (getRcDebug()){
        	args.add("-debug");
        }
        if (getRcTrustAllSSLCerts()){
        	args.add("-trustAllSSLCertificates");
        }
        if (getRcBrowserSessionReuse()) {
        	args.add("-browserSessionReuse");
        }
        addIfHasText(args, "-firefoxProfileTemplate", getRcFirefoxProfileTemplate());
        for (BrowserDescriptor b : browsers) {
        	args.addAll(b.getArgs());
        }
        return args;
	}
	
	private void addIfHasText(List<String> list, String option, String value) {
		if (StringUtils.hasText(value)) {
			list.add(option);
			list.add(value);
		}
	}

	public DescriptorExtensionList<Browser, BrowserDescriptor> getBrowserTypes() {
		return Browser.all();
	}
	
	@Extension
	public static class DescriptorImpl extends ConfigurationDescriptor {

		@Override
		public String getDisplayName() {
			return "Custom configuration";
		}
		
		public DescriptorExtensionList<Browser, BrowserDescriptor> getBrowserTypes() {
			return Browser.all();
		}

	}
	
}
