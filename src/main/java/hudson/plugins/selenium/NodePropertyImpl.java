package hudson.plugins.selenium;

import java.util.ArrayList;
import java.util.List;

import hudson.Extension;
import hudson.model.Node;
import hudson.plugins.selenium.configuration.Configuration;
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
	
	private int port = 4444;
	Configuration configType;
	
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
