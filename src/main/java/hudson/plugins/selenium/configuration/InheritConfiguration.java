package hudson.plugins.selenium.configuration;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.Hudson;
import hudson.model.Hudson.MasterComputer;
import hudson.plugins.selenium.NodePropertyImpl;
import hudson.plugins.selenium.SeleniumRunOptions;

import java.util.logging.Logger;

import org.kohsuke.stapler.DataBoundConstructor;

public class InheritConfiguration extends Configuration {

	@DataBoundConstructor
	public InheritConfiguration() {
		
	}
	
	@Extension	
	public static class DescriptorImpl extends ConfigurationDescriptor {
	
		@Override
		public String getDisplayName() {
			return "Inherit configuration";
		}
	}

	@Override
	public SeleniumRunOptions initOptions(Computer c) {
		if (c instanceof MasterComputer) {
			LOGGER.fine("Master node is excluded from Selenium Grid because it is configured with an inherit configuration. From which configuration is it supposed to inherit ?");
			return null;
		}
        NodePropertyImpl np = Hudson.getInstance().getGlobalNodeProperties().get(NodePropertyImpl.class);
        if (np == null || np.getConfigurationType() == null)
        	return null;
        
        if (np.getConfigurationType() instanceof InheritConfiguration) {
        	LOGGER.fine("Node " + c.getNode().getNodeName() + " is excluded from Selenium Grid because it is configured with an inherit configuration and Master is also configured as inherit. From which configuration is it supposed to inherit ?");
        	return null;
        }
        
        return np.getConfigurationType().initOptions(c);
	}

	
	private static final Logger LOGGER = Logger.getLogger(InheritConfiguration.class.getName());
}
