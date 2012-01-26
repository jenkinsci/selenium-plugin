package hudson.plugins.selenium;

import java.util.ArrayList;
import java.util.List;

import hudson.Extension;
import hudson.model.Node;
import hudson.plugins.selenium.configuration.ConfigurationType;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Marker property to ...
 *
 * @author Richard Lavoie
 */
@ExportedBean
public class NodePropertyImpl extends NodeProperty<Node> {
	
	ConfigurationType configType;
	
    @DataBoundConstructor
    public NodePropertyImpl() {}
    
    @Exported
    public ConfigurationType getConfigurationType() {
    	return configType;
    }
    
	public List<String> getUserArgs() {
		if (configType == null) return new ArrayList<String>();
		return configType.getLaunchingArguments();
	}

    @Extension
    public static class DescriptorImpl extends NodePropertyDescriptor {
        @Override
        public String getDisplayName() {
            return "Enable Selenium Grid on this node";
        }
    }
}
