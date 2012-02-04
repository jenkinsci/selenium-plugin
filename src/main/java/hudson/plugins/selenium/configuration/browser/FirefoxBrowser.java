package hudson.plugins.selenium.configuration.browser;

import hudson.Extension;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;
import org.openqa.selenium.remote.BrowserType;


public class FirefoxBrowser extends Browser {
	
	transient final protected String PARAM_BINARY_PATH = "firefox_binary";
	
	transient private static final String BROWSER_NAME = "firefox";
	
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
		return BROWSER_NAME;
	}
	
	public List<String> getAdditionnalArgs() {
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
