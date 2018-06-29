package hudson.plugins.selenium;

import com.beust.jcommander.JCommander;
import hudson.remoting.Channel;
import org.openqa.grid.internal.cli.GridHubCliOptions;
import org.openqa.grid.internal.cli.GridNodeCliOptions;
import org.openqa.grid.internal.utils.configuration.GridHubConfiguration;
import org.openqa.grid.internal.utils.configuration.GridNodeConfiguration;

/**
 * Converts String[] args to Selenium Grid configuration objects using {@link JCommander}
 */
public class ConfigurationBuilder {

	public static GridNodeConfiguration buildNodeConfig(String[] args) {
		return new GridNodeCliOptions().parse(args).toConfiguration();
	}

	public static GridHubConfiguration buildHubConfig(String[] args, Integer port) {
		GridHubConfiguration config = new GridHubCliOptions().parse(args).toConfiguration();
		config.port = port;
		config.capabilityMatcher = new JenkinsCapabilityMatcher(Channel.current(), config.capabilityMatcher);
		return config;
	}
}
