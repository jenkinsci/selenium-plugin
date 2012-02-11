package hudson.plugins.selenium.configuration.browser;

import hudson.Extension;

import java.util.HashMap;
import java.util.Map;

import org.kohsuke.stapler.DataBoundConstructor;


public class OperaBrowser extends Browser {
	
	transient final protected String PARAM_BINARY_PATH = "opera.binary";

	transient private static final String BROWSER_NAME = "opera";
	
	String binary;
	
	@DataBoundConstructor
	public OperaBrowser(int maxInstances, String version, String binary, Boolean configuredAsRC) {
		super(maxInstances, version);
		this.binary = binary;
	}
	
	@Override
	public String getBrowserName() {
		return BROWSER_NAME;
	}

	@Override
	public String getRCBrowserName() {
		return null;
	}	

	@Override
	public Map<String, String> getJVMArgs() {
		Map<String, String> args = new HashMap<String, String>();
		combine(args, PARAM_BINARY_PATH, binary);
		return args;
	}

		
    @Extension
    public static class DescriptorImpl extends BrowserDescriptor {

    	@Override
		public String getDisplayName() {
			return "Opera";
		}

    }
}
