package hudson.plugins.selenium.configuration.browser;

import hudson.Extension;

import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;
import org.openqa.selenium.remote.BrowserType;

public class ChromeBrowser extends Browser {
	
	String binary;
	
	@DataBoundConstructor
	public ChromeBrowser(int maxInstances, String version, String binary) {
		super(maxInstances, version, BrowserType.CHROME);
		this.binary = binary;
	}
	
	public String getBinary() {
		return binary;
	}
	
    @Extension
    public static class DescriptorImpl extends BrowserDescriptor {
    	
    	String binary_path;
    	
    	public DescriptorImpl() {
    		super(5, BrowserType.CHROME);
    	}
    	
        @Override
        public String getDisplayName() {
            return "Chrome";
        }
        
    	@Override
    	public List<String> getArgs() {
    		List<String> options = super.getArgs();
    		combine(options, "chrome.binary", binary_path);
    		return options;
    	}

    }
	
	
}
