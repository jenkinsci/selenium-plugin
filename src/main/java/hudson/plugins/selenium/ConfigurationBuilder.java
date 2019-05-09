package hudson.plugins.selenium;

import com.beust.jcommander.JCommander;
import hudson.remoting.Channel;
import org.openqa.grid.internal.cli.GridHubCliOptions;
import org.openqa.grid.internal.cli.GridNodeCliOptions;
import org.openqa.grid.internal.utils.configuration.GridHubConfiguration;
import org.openqa.grid.internal.utils.configuration.GridNodeConfiguration;
import org.openqa.grid.selenium.GridLauncherV3;

/**
 * Converts String[] args to Selenium Grid configuration objects using {@link JCommander}
 */
public class ConfigurationBuilder {

	public static GridNodeConfiguration buildNodeConfig(String[] args) {
		GridNodeCliOptions nodeOptions = new GridNodeCliOptions();
		JCommander.newBuilder().addObject(nodeOptions).build().parse(args);
		return new GridNodeConfiguration(nodeOptions);
	}

	public static GridHubConfiguration buildHubConfig(String[] args, Integer port) {
		GridHubCliOptions cliOptions = new GridHubCliOptions();
		JCommander.newBuilder().addObject(cliOptions).build().parse(args);
		GridHubConfiguration config = new GridHubConfiguration(cliOptions);
		config.port = port;
		config.capabilityMatcher = new JenkinsCapabilityMatcher(Channel.current(), config.capabilityMatcher);
		return config;
	}
}
