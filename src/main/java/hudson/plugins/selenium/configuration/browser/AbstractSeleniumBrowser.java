package hudson.plugins.selenium.configuration.browser;

import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Computer;
import hudson.plugins.selenium.SeleniumRunOptions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.openqa.grid.common.SeleniumProtocol;

@ExportedBean
public abstract class AbstractSeleniumBrowser<T extends AbstractSeleniumBrowser<T>&Describable<T>> implements ExtensionPoint, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1895158524568642537L;

	transient protected final String PARAM_BROWSER_NAME = "browserName";

	transient protected final String PARAM_MAX_INSTANCES = "maxInstances";
	
	transient protected final String PARAM_VERSION = "version";

	transient protected final String PARAM_SELENIUM_PROTOCOL = "seleniumProtocol";
		
	private int maxInstances = 0;
	private String version;
	private SeleniumProtocol protocol;
	private String name;

	protected AbstractSeleniumBrowser(SeleniumProtocol protocol, int instances, String version, String name) {
		if (protocol == null) {
			throw new NullPointerException();
		}
		
		maxInstances = instances;
		this.version = version;
		this.protocol = protocol;
		this.name = name;
	}
	
	@Exported
	public int getMaxInstances() {
		return maxInstances;
	}
	
	@Exported
	public String getVersion() {
		return version;
	}
	
	@Exported 
	public String getName() {
		return name;
	}
	
	@Exported
	public String getProtocol() {
		return protocol.toString();
	}
	
//	public BrowserDescriptor getDescriptor() {
//        return (BrowserDescriptor)Hudson.getInstance().getDescriptor(getClass());
//    }

    /**
     * Combine the key and value on the key=value form and add that form in the options list
     * @param options List on which to add the key=value pair
     * @param key Key option
     * @param value Value option
     */
    protected static void combine(List<String> options, String key, Object value) {
    	if (value != null && !StringUtils.isBlank(value.toString())) {
    		//TODO validate the " in the strings, this is error prone ...
    		options.add(key + "=" + value.toString().replace("\"", "\\\""));
    	}
    }
    
	
    protected static void combine(Map<String, String> args, String key, String value) {
		if (!StringUtils.isBlank(value)) {
			args.put(key, value);
		}
	}

	public void initOptions(Computer c, SeleniumRunOptions opt) {
		opt.getJVMArguments().putAll(getJVMArgs());
		opt.addOption("-browser");
		opt.addOption(StringUtils.join(initBrowserOptions(c, opt), ","));
	}
	
	protected List<String> initBrowserOptions(Computer c, SeleniumRunOptions options) {
		List<String> args = new ArrayList<String>();
        combine(args, PARAM_SELENIUM_PROTOCOL, protocol);
        combine(args, PARAM_BROWSER_NAME, getName());
        combine(args, PARAM_MAX_INSTANCES, maxInstances);
		combine(args, PARAM_VERSION, version);
		return args;
	}

	protected Map<String, String> getJVMArgs() {
		return Collections.emptyMap();
	}
	
}
