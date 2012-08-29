package hudson.plugins.selenium.actions;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.ManagementLink;
import hudson.model.Descriptor;
import hudson.plugins.selenium.configuration.global.SeleniumGlobalConfiguration;
import jenkins.model.Jenkins;

@Extension
public class SeleniumConfigurationAction extends ManagementLink implements Describable<SeleniumConfigurationAction> {

	private SeleniumGlobalConfiguration config;

	public String getIconFileName() {
		return "/plugin/selenium/24x24/selenium.png";
	}

	public String getDisplayName() {
		return "Selenium configurations";
	}

	public String getUrlName() {
		return "selenium/configurations";
	}
	
	public String getDescription() {
		return "Selenium node configurations";
	}

    /**
     * Gets the descriptor.
     * @return descriptor.
     */
    public Descriptor<SeleniumConfigurationAction> getDescriptor() {
        return Jenkins.getInstance().getDescriptorByType(DescriptorImpl.class);
    }
	
    /**
     * Descriptor is only used for auto completion.
     */
    @Extension
    public static final class DescriptorImpl extends Descriptor<SeleniumConfigurationAction> {
        
    	@Override
        public String getDisplayName() {
            return null; //Not used.
        }
        
    }

}
