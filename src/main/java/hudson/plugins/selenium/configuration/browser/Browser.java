package hudson.plugins.selenium.configuration.browser;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Computer;
import hudson.model.Describable;
import hudson.model.Hudson;
import hudson.plugins.selenium.SeleniumRunOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public abstract class Browser implements Describable<Browser>, ExtensionPoint {
	
	transient protected final String PARAM_BROWSER_NAME = "browserName";

	transient protected final String PARAM_MAX_INSTANCES = "maxInstances";
	
	transient protected final String PARAM_VERSION = "version";
	
	transient protected final String PARAM_SELENIUM_PROTOCOL = "seleniumProtocol";
	
	transient protected final String SELENIUM_WD_PROTOCOL = "WebDriver";
	
	transient protected final String SELENIUM_RC_PROTOCOL = "Selenium";
	
	private int maxInstances = 0;
	private String version;
	private Boolean configuredAsRC;

	protected Browser(int instances, String version, Boolean configuredAsRC) {
		maxInstances = instances;
		this.version = version;
		this.configuredAsRC = configuredAsRC;
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
	public Boolean getConfiguredAsRC() {
		return configuredAsRC;
	}

	
	public BrowserDescriptor getDescriptor() {
        return (BrowserDescriptor)Hudson.getInstance().getDescriptor(getClass());
    }

    public static DescriptorExtensionList<Browser,BrowserDescriptor> all() {
        return Hudson.getInstance().<Browser,BrowserDescriptor>getDescriptorList(Browser.class);
    }

    /**
     * Retrieve WebDriver browser name
     * @return Browser name, must be one of {htmlunit, firefox, chrome, opera, internet explorer, android, iphone, and others} 
     */
    public abstract String getBrowserName();

    /**
     * Retrieve RC browser name
     * @return Browser name, must be one of {*firefox, *googlechrome, *iexplorer, and others} 
     */
    public abstract String getRCBrowserName();

    
    /**
     * Combine the key and value on the key=value form and add that form in the options list
     * @param options List on which to add the key=value pair
     * @param key Key option
     * @param value Value option
     */
    protected static void combine(List<String> options, String key, Object value) {
    	if (value != null && !StringUtils.isBlank(value.toString())) {
    		//TODO validate the " in the strings, this is error prone ...
    		options.add(key + "=" + value.toString());
    	}
    }
    
	
    protected static void combine(Map<String, String> args, String key, String value) {
		if (!StringUtils.isBlank(value)) {
			args.put(key, value);
		}
	}

	public void initOptions(Computer c, SeleniumRunOptions opt) {
		List<String> args = new ArrayList<String>();
		if (configuredAsRC == null || !configuredAsRC) {
			combine(args, PARAM_SELENIUM_PROTOCOL, SELENIUM_WD_PROTOCOL);
			combine(args, PARAM_BROWSER_NAME, getBrowserName());
		} else if (!StringUtils.isBlank(getRCBrowserName())){
			combine(args, PARAM_SELENIUM_PROTOCOL, SELENIUM_RC_PROTOCOL);
			combine(args, PARAM_BROWSER_NAME, getRCBrowserName());			
		} else {
			// configured as RC, but the browser is blank, so we don't add it in the startup config
			return;
		}
		combine(args, PARAM_MAX_INSTANCES, maxInstances);
		combine(args, PARAM_VERSION, version);
		args.addAll(getOptions());
		
		opt.getJVMArguments().putAll(getJVMArgs());
		
		List<String> opts = opt.getSeleniumArguments();
		opts.add("-browser");
		
		opts.add(StringUtils.join(args, ","));
	}
	
	public Map<String, String> getJVMArgs() {
		return Collections.emptyMap();
	}

	public List<String> getOptions() {
		return Collections.emptyList();
	}

	
}
