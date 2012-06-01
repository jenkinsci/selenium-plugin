package hudson.plugins.selenium.configuration;

import hudson.DescriptorExtensionList;
import hudson.model.Describable;
import hudson.model.Computer;
import hudson.model.Hudson;
import hudson.model.Node;
import hudson.plugins.selenium.SeleniumRunOptions;

import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public abstract class Configuration implements Describable<Configuration> {

	protected String display = null;

	public ConfigurationDescriptor getDescriptor() {
        return (ConfigurationDescriptor)Hudson.getInstance().getDescriptor(getClass());
    }

    public static DescriptorExtensionList<Configuration,ConfigurationDescriptor> all() {
        return Hudson.getInstance().<Configuration,ConfigurationDescriptor>getDescriptorList(Configuration.class);
    }
	
    public static DescriptorExtensionList<Configuration,ConfigurationDescriptor> allExcept(Node current) {
        return Hudson.getInstance().<Configuration,ConfigurationDescriptor>getDescriptorList(Configuration.class);
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
	
}
