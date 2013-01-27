package hudson.plugins.selenium.configuration.browser.selenium;

import hudson.Extension;
import hudson.util.FormValidation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.export.Exported;

public class ChromeBrowser extends SeleniumBrowser {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7028484889764200348L;

	transient final protected String PARAM_BINARY_PATH = "webdriver.chrome.driver";
		
	private String binary;
	
	@DataBoundConstructor
	public ChromeBrowser(int maxInstances, String version, String binary) {
		super(maxInstances, version, "*googlechrome");
		this.binary = binary;
	}
	
	@Exported
	public String getBinary() {
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
    	
    	public String getMaxInstances() {
    		return "5";
    	}
    	
        @Override
        public String getDisplayName() {
            return "Chrome";
        }
        
        public FormValidation doCheckBinary(@QueryParameter String value) throws IOException, ServletException {
        	if (StringUtils.isBlank(value)) {
        		return FormValidation.warning("Must not be empty unless it is already defined from a previous chrome browser definition");
        	}
        	return FormValidation.ok();
        }


    }

}
