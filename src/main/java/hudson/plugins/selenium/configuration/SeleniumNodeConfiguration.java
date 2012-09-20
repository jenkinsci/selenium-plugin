package hudson.plugins.selenium.configuration;

import hudson.DescriptorExtensionList;
import hudson.model.Describable;
import hudson.model.Computer;
import hudson.model.Hudson;
import hudson.model.Node;
import hudson.plugins.selenium.SeleniumRunOptions;

import java.io.Serializable;

import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public abstract class SeleniumNodeConfiguration implements Describable<SeleniumNodeConfiguration>, Serializable{

	private String displayName = null;

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
		if (displayName == null) {
			String name = getClass().getSimpleName();
			StringBuffer b = new StringBuffer(name.length());
			b.append(name.charAt(0));
			for (int i = 1; i < name.length(); i++) {
				if (Character.isUpperCase(name.charAt(i))) {
					b.append(" ");	
				}
		        b.append(name.charAt(i));
		    }
			displayName = b.toString();
		}
		return displayName;
	}

	
	public String getIcon() {
		return "/images/24x24/gear.png";
	}
	
	public String getIconAltText() {
		return getDescriptor().getDisplayName();
	}
	
}
