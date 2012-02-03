package hudson.plugins.selenium.configuration;

import hudson.Extension;
import hudson.model.Computer;
import hudson.plugins.selenium.SeleniumRunOptions;

import org.kohsuke.stapler.DataBoundConstructor;

public class FileConfiguration extends Configuration {

	private String fileURL;
	
    @DataBoundConstructor
    public FileConfiguration(String fileURL) {
    	this.fileURL = fileURL;
    }
    
    public String getFileURL() {
    	return fileURL;
    }
    
	@Extension
	public static class DescriptorImpl extends ConfigurationDescriptor {

		@Override
		public String getDisplayName() {
			return "File configuration";
		}
	}

	@Override
	public SeleniumRunOptions initOptions(Computer c) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
