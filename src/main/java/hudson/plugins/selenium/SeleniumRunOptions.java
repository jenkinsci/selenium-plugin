package hudson.plugins.selenium;

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
public class SeleniumRunOptions {

	private List<String> arguments;
	
	private Map<String, String> jvmArgs;
	
	public SeleniumRunOptions() {
		arguments = new ArrayList<String>();
		jvmArgs = new HashMap<String, String>();
	}
	
	public List<String> getSeleniumArguments() {
		return arguments;
	}
	
	public Map<String, String> getJVMArguments() {
		return jvmArgs;
	}
	
	public void addOption(String option) {
		arguments.add(option);
	}
	
	public void addOptionIfSet(String option, String value) {
		if (StringUtils.hasText(value)) {
			arguments.add(option);
			arguments.add(value);
		}
	}
	
}
