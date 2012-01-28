package hudson.plugins.selenium;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.model.Hudson;
import hudson.model.Node;
import hudson.plugins.selenium.configuration.Configuration;
import hudson.plugins.selenium.configuration.ConfigurationDescriptor;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.tools.ToolDescriptor;

import java.util.ArrayList;
import java.util.List;

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
	
	private int port = 4444;
	Configuration configType;
	
    /**
     * Returns all the registered {@link ToolDescriptor}s.
     */
    public static DescriptorExtensionList<Configuration,ConfigurationDescriptor> configTypes() {
        // use getDescriptorList and not getExtensionList to pick up legacy instances
        return Hudson.getInstance().<Configuration,ConfigurationDescriptor>getDescriptorList(Configuration.class);
    }
    
	
	
    @DataBoundConstructor
    public NodePropertyImpl() {}
    
    @Exported
    public Configuration getConfigurationType() {
    	return configType;
    }
    
    @Exported
    public int getPort() {
    	return port;
    }
    
	public List<String> getUserArgs() {
		if (configType == null) return new ArrayList<String>();
		return null;
		//return configType.getLaunchingArguments();
	}

    @Extension
    public static class DescriptorImpl extends NodePropertyDescriptor {
        @Override
        public String getDisplayName() {
            return "Enable Selenium Grid on this node";
        }
    }
}
