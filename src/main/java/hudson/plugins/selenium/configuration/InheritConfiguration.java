package hudson.plugins.selenium.configuration;

import hudson.model.Hudson;
import hudson.plugins.selenium.NodePropertyImpl;

import java.util.List;

public class InheritConfiguration extends ConfigurationDescriptor {

	@Override
	public List<String> getLaunchingArguments() {
        NodePropertyImpl np = Hudson.getInstance().getNodeProperties().get(NodePropertyImpl.class);
        return null;
        //return (np == null || np.getConfigurationType() == null ? null : np.getConfigurationType().getLaunchingArguments());
	}

	@Override
	public String getDisplayName() {
		return "Inherit configuration";
	}
	
}
