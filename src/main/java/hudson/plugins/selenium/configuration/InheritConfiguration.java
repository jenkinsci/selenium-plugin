package hudson.plugins.selenium.configuration;

import hudson.model.Hudson;
import hudson.plugins.selenium.NodePropertyImpl;

import java.util.List;

public class InheritConfiguration extends ConfigurationType {

	@Override
	public List<String> getLaunchingArguments() {
        NodePropertyImpl np = Hudson.getInstance().getNodeProperties().get(NodePropertyImpl.class);
        return (np == null || np.getConfigurationType() == null ? null : np.getConfigurationType().getLaunchingArguments());
	}
	
}
