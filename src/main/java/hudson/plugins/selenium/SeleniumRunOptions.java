package hudson.plugins.selenium;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;


/**
 * This is only a bean that contains the JVM settings and the selenium arguments to start
 *  
 * @author Richard Lavoie
 *
 */
public final class SeleniumRunOptions implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5571585840451096944L;

	private List<String> arguments;
	
	private Map<String, String> jvmArgs;
	
	private Map<String, String> envVars;
	
	public SeleniumRunOptions() {
		arguments = new ArrayList<String>();
		jvmArgs = new HashMap<String, String>();
		envVars = new HashMap<String, String>();
	}
	
	/**
	 * Returns the selenium process arguments 
	 * 
	 * @return List of arguments
	 */
	public List<String> getSeleniumArguments() {
		return arguments;
	}
	
	/**
	 * Returns the jvm arguments that needs to be given to the RcVM class (-D options mostly)
	 * 
	 * @return Map of key/value options to be given to the jvm starting selenium
	 */
	public Map<String, String> getJVMArguments() {
		return jvmArgs;
	}
	
	/**
	 * Add an option to the selenium process' command line arguments list 
	 * @param option Option to add
	 */
	public void addOption(String option) {
		arguments.add(option);
	}
	
	/**
	 * Add an option only if the value is set : not null and length > 0
	 * 
	 * @param option Option to set
	 * @param value Value of the option
	 */
	public void addOptionIfSet(String option, Object value) {
		if (value != null && StringUtils.hasText(value.toString())) {
			arguments.add(option);
			arguments.add(value.toString());
		}
	}

	public Map<String, String> getEnvironmentVariables() {
		return envVars;
	}
	
	/**
	 * Sets the environment variables to the specified value. If the value is null, the environment variable is removed.
	 * @param key Variable name
	 * @param value Variable value
	 */
	public void setEnvVar(String key, String value) {
		if (value == null) {
			envVars.remove(key);
		} else {
			envVars.put(key, value);
		}
	}
	
	public static SeleniumRunOptions merge(SeleniumRunOptions options1, SeleniumRunOptions options2) {
		if (options1 == null && options2 == null)
			return null;
		SeleniumRunOptions newOpts = new SeleniumRunOptions();
		addAllOptions(newOpts, options1);
		addAllOptions(newOpts, options2);
		
		return newOpts;
	}

	private static void addAllOptions(SeleniumRunOptions newOpts, SeleniumRunOptions options) {
		if (options == null) {
			return;
		}
		for (String arg : options.arguments) {
			newOpts.arguments.add(arg);
		}
		for (Map.Entry<String, String> arg : options.jvmArgs.entrySet()) {
			newOpts.jvmArgs.put(arg.getKey(), arg.getValue());
		}
		for (Map.Entry<String, String> arg : options.envVars.entrySet()) {
			newOpts.envVars.put(arg.getKey(), arg.getValue());
		}
	}
}
