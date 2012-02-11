package hudson.plugins.selenium.configuration.browser;

import hudson.Extension;

import java.util.HashMap;
import java.util.Map;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;

public class ChromeBrowser extends Browser {
	
	transient final protected String PARAM_BINARY_PATH = "webdriver.chrome.driver";
	
	transient private static final String WD_BROWSER_NAME = "chrome";
	
	transient private static final String RC_BROWSER_NAME = "*googlechrome";
	
	private String binary;
	
	@DataBoundConstructor
	public ChromeBrowser(int maxInstances, String version, String binary) {
		super(maxInstances, version);
		this.binary = binary;
	}
	
	@Exported
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

	@Override
	public Map<String, String> getJVMArgs() {
		 Map<String, String> args = new HashMap<String, String>();
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
