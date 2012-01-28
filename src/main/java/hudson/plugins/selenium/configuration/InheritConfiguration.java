package hudson.plugins.selenium.configuration;

import hudson.Extension;
import hudson.model.Hudson;
import hudson.plugins.selenium.NodePropertyImpl;

import java.util.List;

public class InheritConfiguration extends Configuration {

	@Override
	public List<String> getLaunchingArguments() {
        NodePropertyImpl np = Hudson.getInstance().getNodeProperties().get(NodePropertyImpl.class);
        return null;
        //return (np == null || np.getConfigurationType() == null ? null : np.getConfigurationType().getLaunchingArguments());
	}

	@Extension	
	public static class DescriptorImpl extends ConfigurationDescriptor {
	
		@Override
		public String getDisplayName() {
			return "Inherit configuration";
		}
	}
	
}
