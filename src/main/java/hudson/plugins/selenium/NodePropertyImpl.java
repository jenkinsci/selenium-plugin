package hudson.plugins.selenium;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.plugins.selenium.configuration.Configuration;
import hudson.plugins.selenium.configuration.ConfigurationDescriptor;
import hudson.plugins.selenium.configuration.InheritConfiguration;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;

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

	Configuration configType;

	@DataBoundConstructor
	public NodePropertyImpl(Configuration configuration) {
		configType = configuration;
	}

	@Exported
	public Configuration getConfigurationType() {
		return configType;
	}

	public List<String> getUserArgs() {
		if (configType == null)
			return new ArrayList<String>();
		return null;
		// return configType.getLaunchingArguments();
	}

	@Extension
	public static class DescriptorImpl extends NodePropertyDescriptor {

		Configuration configType;
		
		@Override
		public String getDisplayName() {
			return "Enable Selenium Grid on this node";
		}

		public DescriptorExtensionList<Configuration, ConfigurationDescriptor> getTypes() {
			return Configuration.all();
		}

		@Override
		public NodeProperty<?> newInstance(StaplerRequest req, JSONObject json) {
			Configuration conf = null;
			try {
				// TODO: FIX THIS SH...
				json.element("stapler-class", json.getJSONArray("stapler-class").get(json.getJSONObject("configuration").getInt("value")));
				conf = Configuration.all().get(json.getJSONObject("configuration").getInt("value")).newInstance(req, json);
			} catch (FormException e) {
				e.printStackTrace();
			}
			return new NodePropertyImpl(conf);
		}
		
		/**
		 * @return default configuration for nodes
		 */
		public Configuration getDefaultConfiguration() {
			return DEFAULT_CONFIGURATION;
		}

		public static final Configuration DEFAULT_CONFIGURATION = new InheritConfiguration();

	}

	public SeleniumRunOptions initOptions(Computer c) {
		return configType.initOptions(c);
	}
}