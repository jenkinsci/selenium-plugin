package hudson.plugins.selenium.configuration.browser;

import hudson.Extension;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;

public class ChromeBrowser extends Browser {
	
	transient final protected String PARAM_BINARY_PATH = "chrome.binary";
	
	transient private static final String WD_BROWSER_NAME = "chrome";
	
	transient private static final String RC_BROWSER_NAME = "*googlechrome";
	
	private String binary;
	
	@DataBoundConstructor
	public ChromeBrowser(int maxInstances, String version, String binary, Boolean configuredAsRC) {
		super(maxInstances, version, configuredAsRC);
		this.binary = binary;
	}
	
	public String getBinary() {
		return binary;
	}
	
	
	@Override
	public String getBrowserName() {
		return WD_BROWSER_NAME;
	}
	
	@Override
	public String getRCBrowserName() {
		return RC_BROWSER_NAME;
	}	

	public List<String> getAdditionnalArgs() {
		List<String> args = new ArrayList<String>();
		combine(args, PARAM_BINARY_PATH, binary);
		return args;
	}
	
    @Extension
    public static class DescriptorImpl extends BrowserDescriptor {
    	
    	public String getMaxInstances() {
    		return "5";
    	}
    	
        @Override
        public String getDisplayName() {
            return "Chrome";
        }

    }
}
