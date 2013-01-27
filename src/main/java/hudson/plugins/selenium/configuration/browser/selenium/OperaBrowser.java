package hudson.plugins.selenium.configuration.browser.selenium;

import hudson.Extension;

import java.util.HashMap;
import java.util.Map;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;


public class OperaBrowser extends SeleniumBrowser {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7520649475709638350L;

	transient final protected String PARAM_BINARY_PATH = "opera.binary";

	private String binary;
	
	@DataBoundConstructor
	public OperaBrowser(int maxInstances, String version, String binary, Boolean configuredAsRC) {
		super(maxInstances, version, "*opera");
		this.binary = binary;
	}
	
	@Exported
	public String getBrowserBinary() {
		return binary;
	}
	
	@Override
	public Map<String, String> getJVMArgs() {
		Map<String, String> args = new HashMap<String, String>();
		combine(args, PARAM_BINARY_PATH, binary);
		return args;
	}

	
		
    @Extension
    public static class DescriptorImpl extends SeleniumBrowserDescriptor {

    	@Override
		public String getDisplayName() {
			return "Opera";
		}

    }
}
