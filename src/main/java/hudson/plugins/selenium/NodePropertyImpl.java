package hudson.plugins.selenium;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.model.Computer;
import hudson.model.Hudson;
import hudson.model.Hudson.MasterComputer;
import hudson.model.Node;
import hudson.plugins.selenium.callables.DeepLevelCallable;
import hudson.plugins.selenium.callables.RemoteGetStatus;
import hudson.plugins.selenium.callables.SeleniumConstants;
import hudson.plugins.selenium.configuration.Configuration;
import hudson.plugins.selenium.configuration.ConfigurationDescriptor;
import hudson.plugins.selenium.configuration.CustomConfiguration;
import hudson.plugins.selenium.configuration.InheritConfiguration;
import hudson.plugins.selenium.configuration.browser.Browser;
import hudson.plugins.selenium.configuration.browser.ChromeBrowser;
import hudson.plugins.selenium.configuration.browser.FirefoxBrowser;
import hudson.plugins.selenium.configuration.browser.IEBrowser;
import hudson.remoting.VirtualChannel;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Marker property to ...
 * 
 * @author Richard Lavoie
 */
@ExportedBean
public class NodePropertyImpl extends NodeProperty<Node> {

	private Configuration configType;
	private transient VirtualChannel channel;

	@DataBoundConstructor
	public NodePropertyImpl(Configuration configuration) {
		configType = configuration;
	}

	@Exported
	public Configuration getConfigurationType() {
		return configType;
	}
	
	@Extension
	public static class DescriptorImpl extends NodePropertyDescriptor {

		@Override
		public String getDisplayName() {
			return "Enable Selenium Grid on this node";
		}

		public DescriptorExtensionList<Configuration, ConfigurationDescriptor> getTypes() {
			return Configuration.all();
		}
		
		@Override
		public NodeProperty<?> newInstance(StaplerRequest req, JSONObject json) throws FormException {
			if (json.has("configuration")) {
				NodePropertyImpl np = Hudson.getInstance().getGlobalNodeProperties().get(NodePropertyImpl.class);
				
				Configuration configType = Configuration.all().newInstanceFromRadioList(json.getJSONObject("configuration"));
				
				if (np != null && (np.getConfigurationType() == null || np.getConfigurationType().getClass() == InheritConfiguration.class) && configType.getClass() == InheritConfiguration.class) {
					throw new FormException("You cannot define an inherit configuration if the master has no configuration or the master is set with an inherit configuration.", "configuration");
				}
				
				return new NodePropertyImpl(configType);
			}
			throw new FormException("You must choose the selenium configuration type for the node", "configuration");
		}
		
		/**
		 * @return default configuration for nodes
		 */
		public Configuration getConfigurationType() {
			return DEFAULT_CONFIGURATION;
		}

		public static final Configuration DEFAULT_CONFIGURATION;
		
		static {
			List<Browser> browsers = new ArrayList<Browser>();
			browsers.add(new ChromeBrowser(5, null, null));
			browsers.add(new FirefoxBrowser(5, null, null));
			browsers.add(new IEBrowser(1, null, null));
			
			
			DEFAULT_CONFIGURATION = new CustomConfiguration(4445, false, false, false, false, -1, "", browsers, null); 
		}

	}

	public SeleniumRunOptions initOptions(Computer c) {
		return configType.initOptions(c);
	}

	public void setChannel(VirtualChannel channel) {
		this.channel = channel;
	}

	public VirtualChannel getChannel() {
		return channel;
	}
	
	public String getStatus() {
		try {
			if (channel == null) return SeleniumConstants.STOPPED;
			return channel.call(new RemoteGetStatus());
		} catch (Throwable e) {
			return "An error occured while retrieving the status of the selenium process " + e.getMessage();
		}
	}
	
	public static NodePropertyImpl getNodeProperty(Computer c) {
		NodePropertyImpl np;
        if (c instanceof MasterComputer) {
        	np = Hudson.getInstance().getGlobalNodeProperties().get(NodePropertyImpl.class);
        } else {
        	np = c.getNode().getNodeProperties().get(NodePropertyImpl.class);
        }        
        return np;
	}
	
	public boolean isShowable(Computer c) {
		if (c instanceof MasterComputer) {
			return true;
		} else {
			return !equals(Hudson.getInstance().getGlobalNodeProperties().get(NodePropertyImpl.class));
		}
	}
	
}