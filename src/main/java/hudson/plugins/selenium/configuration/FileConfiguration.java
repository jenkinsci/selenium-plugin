package hudson.plugins.selenium.configuration;

import hudson.Extension;

import java.util.List;

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
    
	@Override
	public List<String> getLaunchingArguments() {
		return null;
	}

	@Extension
	public static class DescriptorImpl extends ConfigurationDescriptor {

		@Override
		public String getDisplayName() {
			return "File configuration";
		}
	}
	
}
