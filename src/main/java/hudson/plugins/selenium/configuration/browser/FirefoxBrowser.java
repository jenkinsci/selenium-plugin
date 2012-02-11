package hudson.plugins.selenium.configuration.browser;

import hudson.Extension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;


public class FirefoxBrowser extends Browser {
	
	transient final protected String PARAM_BINARY_PATH = "firefox_binary";
	
	transient private static final String WD_BROWSER_NAME = "firefox";
	
	transient private static final String RC_BROWSER_NAME = "*firefox";
	
	private String binary_path;
	
	@DataBoundConstructor
	public FirefoxBrowser(int maxInstances, String version, String binary) {
		super(maxInstances, version);
		binary_path = binary;
	}
	
	@Exported
	public String getBinaryPath() {
		return binary_path;
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
	public List<String> getWDOptions() {
		List<String> args = new ArrayList<String>();
		combine(args, PARAM_BINARY_PATH, binary_path);
		return args;
	}
		
    @Extension
    public static class DescriptorImpl extends BrowserDescriptor {
    	
		@Override
		public String getDisplayName() {
			return "Firefox";
		}
	
    }
}
