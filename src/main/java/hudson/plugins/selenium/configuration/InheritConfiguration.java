package hudson.plugins.selenium.configuration;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.Hudson;
import hudson.plugins.selenium.NodePropertyImpl;
import hudson.plugins.selenium.SeleniumRunOptions;

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
        NodePropertyImpl np = Hudson.getInstance().getGlobalNodeProperties().get(NodePropertyImpl.class);
        return (np == null || np.getConfigurationType() == null ? null : np.getConfigurationType().initOptions(c));
	}
	
}
