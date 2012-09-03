package hudson.plugins.selenium.configuration;

import hudson.Extension;
import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.model.Computer;
import hudson.plugins.selenium.SeleniumRunOptions;
import hudson.remoting.VirtualChannel;
import hudson.util.IOException2;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;

public class FileConfiguration extends SeleniumNodeConfiguration {

	private String configURL;
	
	private String display;
	
    @DataBoundConstructor
    public FileConfiguration(String configURL, String display) {
    	this.configURL = configURL;
    	this.display = display;
    }
    
    @Exported
    public String getConfigURL() {
    	return configURL;
    }
    
    @Exported
    public String getDisplay() {
    	return display;
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
		SeleniumRunOptions opt = new SeleniumRunOptions();
		try {
			final String filename = "selenium-temp-config-" + System.currentTimeMillis() + ".json";
			
			String fullPath = c.getNode().getRootPath().act(new FileCallable<String>() {

				/**
				 * 
				 */
				private static final long serialVersionUID = -288688398601004624L;

				public String invoke(File f, VirtualChannel channel) throws IOException {
					File conf = new File(f, filename);
					
					FilePath urlConf = new FilePath(conf);
					try {
						urlConf.copyFrom(new URL(configURL));
					} catch (InterruptedException e) {
						throw new IOException2("Failed to retrieve configuration from " + configURL, e);
					}

					
					return conf.getAbsolutePath();
				}
			});

			opt.addOptionIfSet("-nodeConfig", fullPath);
			
			if (display != null && !display.equals("")) {
	        	opt.setEnvVar("DISPLAY", display);
	        }
			
			return opt;
		} catch (Exception e) {
			LOGGER.fine("Cannot download the specified configuration file on the node. " + e.getMessage());
			return null;
		}
	}

	private static final Logger LOGGER = Logger.getLogger(FileConfiguration.class.getName());

	public String getIcon() {
		return "/images/24x24/document.png";
	}
	
		
}
