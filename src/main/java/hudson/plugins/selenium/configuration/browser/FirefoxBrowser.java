package hudson.plugins.selenium.configuration.browser;

import hudson.Extension;

import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;
import org.openqa.selenium.remote.BrowserType;


public class FirefoxBrowser extends Browser {
	
	
	@DataBoundConstructor
	public FirefoxBrowser(int maxInstances, String version, String binary) {
		super(maxInstances, version, BrowserType.FIREFOX);
	}
	
    @Extension
    public static class DescriptorImpl extends BrowserDescriptor {

    	String binary_path;
    	
    	public DescriptorImpl() {
    		super(5, BrowserType.FIREFOX);
    	}
    	
		@Override
		public String getDisplayName() {
			return "Firefox";
		}
	
		@Override
		public List<String> getArgs() {
    		List<String> options = super.getArgs();
    		combine(options, "firefox_binary", binary_path);
    		return options;
		}

	
    }
}
