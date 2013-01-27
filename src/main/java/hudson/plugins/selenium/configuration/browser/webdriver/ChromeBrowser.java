package hudson.plugins.selenium.configuration.browser.webdriver;

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

public class ChromeBrowser extends WebDriverBrowser {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8505665387429684157L;

	transient final protected String PARAM_BINARY_PATH = "webdriver.chrome.driver";
	
	private String server_binary;
	
	@DataBoundConstructor
	public ChromeBrowser(int maxInstances, String version, String server_binary) {
		super(maxInstances, version, "chrome");
		this.server_binary = server_binary;
	}
	
	@Exported
	public String getServer_binary() {
		return server_binary;
	}
	
	@Override
	public Map<String, String> getJVMArgs() {
		 Map<String, String> args = new HashMap<String, String>();
		combine(args, PARAM_BINARY_PATH, server_binary);
		return args;
	}

	@Extension
    public static class DescriptorImpl extends WebDriverBrowserDescriptor {
    	
    	public String getMaxInstances() {
    		return "5";
    	}
    	
        @Override
        public String getDisplayName() {
            return "Chrome";
        }
        
        public FormValidation doCheckServer_binary(@QueryParameter String value) throws IOException, ServletException {
        	if (StringUtils.isBlank(value)) {
        		return FormValidation.warning("Must not be empty unless it is already defined from a previous chrome browser definition");
        	}
        	return FormValidation.ok();
        }


    }
}
