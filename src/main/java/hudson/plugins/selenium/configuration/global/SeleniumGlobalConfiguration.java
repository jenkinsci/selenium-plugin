package hudson.plugins.selenium.configuration.global;

import hudson.model.Computer;
import hudson.plugins.selenium.SeleniumRunOptions;
import hudson.plugins.selenium.configuration.SeleniumNodeConfiguration;
import hudson.plugins.selenium.configuration.global.matcher.SeleniumConfigurationMatcher;

import java.io.Serializable;

import org.kohsuke.stapler.DataBoundConstructor;

public class SeleniumGlobalConfiguration implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8366478398033252973L;

	private String name;
	
	private SeleniumConfigurationMatcher matcher;
	
	private SeleniumNodeConfiguration configuration;
	
	
	@DataBoundConstructor
	public SeleniumGlobalConfiguration(String name, SeleniumConfigurationMatcher matcher, SeleniumNodeConfiguration configuration) {
		this.name = name;
		this.matcher = matcher;
		this.configuration = configuration;
	}
	
	/**
	 * This methods returns the options for a node, null if the underlying matcher doesn't match for this node.
	 * 
	 * @return Selenium startup options
	 */
	public SeleniumRunOptions getOptions(Computer c) {
		if (matcher == null) {
			return null;
		}
		if (!matcher.match(c.getNode())) {
			return null;
		}
		
		return configuration.initOptions(c);
	}
	
	/**
	 * Returns the name of the configuration
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	public String getMatcherSummary() {
		return matcher.getSummary();
	}
	
	public SeleniumNodeConfiguration getConfiguration() {
		return configuration;
	}
	
	public String getConfigSummary() {
		return configuration.getSummary();
	}
	
	public String getDisplayName() {
		return configuration.getDescriptor().getDisplayName();
	}
}
