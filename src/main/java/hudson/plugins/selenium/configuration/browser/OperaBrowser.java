package hudson.plugins.selenium.configuration.browser;

import hudson.Extension;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;
import org.openqa.selenium.remote.BrowserType;


public class OperaBrowser extends Browser {
	
	String binary;
	
	@DataBoundConstructor
	public OperaBrowser(int maxInstances, String version, String binary) {
		super(maxInstances, version);
		this.binary = binary;
	}
	
	@Override
	public String getBrowserName() {
		return BrowserType.OPERA;
	}

	public List<String> getAdditionnalArgs() {
		List<String> args = new ArrayList<String>();
		combine(args, "opera.binary", binary);
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
