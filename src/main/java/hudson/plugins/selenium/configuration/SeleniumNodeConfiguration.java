package hudson.plugins.selenium.configuration;

import java.io.Serializable;

import hudson.DescriptorExtensionList;
import hudson.model.Describable;
import hudson.model.Computer;
import hudson.model.Hudson;
import hudson.model.Node;
import hudson.plugins.selenium.SeleniumRunOptions;

import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public abstract class SeleniumNodeConfiguration implements Describable<SeleniumNodeConfiguration>, Serializable{

	protected String display = null;

	public ConfigurationDescriptor getDescriptor() {
        return (ConfigurationDescriptor)Hudson.getInstance().getDescriptor(getClass());
    }

    public static DescriptorExtensionList<SeleniumNodeConfiguration,ConfigurationDescriptor> all() {
        return Hudson.getInstance().<SeleniumNodeConfiguration,ConfigurationDescriptor>getDescriptorList(SeleniumNodeConfiguration.class);
    }
	
    public static DescriptorExtensionList<SeleniumNodeConfiguration,ConfigurationDescriptor> allExcept(Node current) {
        return Hudson.getInstance().<SeleniumNodeConfiguration,ConfigurationDescriptor>getDescriptorList(SeleniumNodeConfiguration.class);
    }

	public abstract SeleniumRunOptions initOptions(Computer c);

	public String getDisplayName() {
		if (display == null) {
			String name = getClass().getSimpleName();
			StringBuffer b = new StringBuffer(name.length());
			b.append(name.charAt(0));
			for (int i = 1; i < name.length(); i++) {
				if (Character.isUpperCase(name.charAt(i))) {
					b.append(" ");	
				}
		        b.append(name.charAt(i));
		    }
			display = b.toString();
		}
		return display;
	}

	public abstract String getSummary();
	
}
