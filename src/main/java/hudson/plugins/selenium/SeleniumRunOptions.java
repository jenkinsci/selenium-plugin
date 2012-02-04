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
public class SeleniumRunOptions implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5571585840451096944L;

	private List<String> arguments;
	
	private Map<String, String> jvmArgs;
	
	public SeleniumRunOptions() {
		arguments = new ArrayList<String>();
		jvmArgs = new HashMap<String, String>();
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
	 * Add an option to the selenium process' arguments list 
	 * @param option
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
	public void addOptionIfSet(String option, String value) {
		if (StringUtils.hasText(value)) {
			arguments.add(option);
			arguments.add(value);
		}
	}
	
}
