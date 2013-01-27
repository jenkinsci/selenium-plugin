package hudson.plugins.selenium.configuration.browser.selenium;

import hudson.Extension;
import hudson.model.Computer;
import hudson.plugins.selenium.SeleniumRunOptions;

import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;


public class FirefoxBrowser extends SeleniumBrowser {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1180910636911313608L;

	transient final protected String PARAM_BINARY_PATH = "firefox_binary";
	
	private String binary_path;
	
	@DataBoundConstructor
	public FirefoxBrowser(int maxInstances, String version, String binary) {
		super(maxInstances, version, "*firefox");
		binary_path = binary;
	}
	
	@Exported
	public String getBinaryPath() {
		return binary_path;
	}
	
	@Override
	public List<String> initBrowserOptions(Computer c, SeleniumRunOptions options) {
		List<String> args = super.initBrowserOptions(c, options);
		combine(args, PARAM_BINARY_PATH, binary_path);
		return args;
	}
		
    @Extension
    public static class DescriptorImpl extends SeleniumBrowserDescriptor {
    	
		@Override
		public String getDisplayName() {
			return "Firefox";
		}
	
    }
}
