package hudson.plugins.selenium.configuration.browser;

import hudson.Extension;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;


public class OperaBrowser extends Browser {
	
	transient final protected String PARAM_BINARY_PATH = "opera.binary";

	transient private static final String BROWSER_NAME = "opera";
	
	String binary;
	
	@DataBoundConstructor
	public OperaBrowser(int maxInstances, String version, String binary, Boolean configuredAsRC) {
		super(maxInstances, version, configuredAsRC);
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

	public List<String> getAdditionnalArgs() {
		List<String> args = new ArrayList<String>();
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
