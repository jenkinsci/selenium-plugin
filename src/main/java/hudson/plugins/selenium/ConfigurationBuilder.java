package hudson.plugins.selenium;

import com.beust.jcommander.JCommander;
import hudson.remoting.Channel;
import org.apache.commons.lang3.StringUtils;
import org.openqa.grid.internal.utils.configuration.GridHubConfiguration;
import org.openqa.grid.internal.utils.configuration.GridNodeConfiguration;
import org.openqa.grid.internal.utils.configuration.StandaloneConfiguration;

/**
 * Converts String[] args to Selenium Grid configuration objects using {@link JCommander}
 */
public class ConfigurationBuilder {

	public static GridNodeConfiguration buildNodeConfig(String[] args) {
		GridNodeConfiguration config = new GridNodeConfiguration();
		return readConfig(args, config, config.nodeConfigFile);
	}

	public static GridHubConfiguration buildHubConfig(String[] args, Integer port) {
		GridHubConfiguration config = new GridHubConfiguration();
		config = readConfig(args, config, config.hubConfig);
		config.port = port;
		config.capabilityMatcher = new JenkinsCapabilityMatcher(Channel.current(), config.capabilityMatcher);
		return config;
	}

	private static <T extends StandaloneConfiguration> T readConfig(String[] args, T config, String configFile) {
		new JCommander(config, args);
		if (StringUtils.isNotBlank(configFile)) {
			config.merge(GridNodeConfiguration.loadFromJSON(configFile));
		}
		return config;
	}

}
