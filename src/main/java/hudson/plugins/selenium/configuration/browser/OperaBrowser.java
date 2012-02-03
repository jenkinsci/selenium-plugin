package hudson.plugins.selenium.configuration.browser;

import hudson.Extension;

import java.util.List;

import org.openqa.selenium.remote.BrowserType;


public class OperaBrowser extends Browser {
	
	
	
    @Extension
    public static class DescriptorImpl extends BrowserDescriptor {

    	String binary_path;
    	
    	public DescriptorImpl() {
    		super(5, BrowserType.OPERA);
    	}
    	
		@Override
		public String getDisplayName() {
			return "Opera";
		}
	
		@Override
		public List<String> getArgs() {
    		List<String> options = super.getArgs();
    		combine(options, "opera.binary", binary_path);
    		return options;
		}

	
    }
}
