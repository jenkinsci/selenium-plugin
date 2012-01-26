package hudson.plugins.selenium.configuration;

import java.util.List;

import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public abstract class ConfigurationType {

	public abstract List<String> getLaunchingArguments();

	
	
}
