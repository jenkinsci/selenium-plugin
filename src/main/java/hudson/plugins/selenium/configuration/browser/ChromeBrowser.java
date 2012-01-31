package hudson.plugins.selenium.configuration.browser;

import hudson.Extension;

import java.util.List;

public class ChromeBrowser extends Browser {
	
    @Extension
    public static class DescriptorImpl extends BrowserDescriptor {
        @Override
        public String getDisplayName() {
            return "Chrome";
        }
        
    	@Override
    	public List<String> getArgs() {
    		return null;
    	}

    }
	
	
}
