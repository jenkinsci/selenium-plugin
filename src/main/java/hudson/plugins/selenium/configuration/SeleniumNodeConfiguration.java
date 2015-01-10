package hudson.plugins.selenium.configuration;

import hudson.DescriptorExtensionList;
import hudson.model.Describable;
import hudson.model.Computer;
import hudson.model.Hudson;
import hudson.model.Node;
import hudson.plugins.selenium.process.SeleniumJarRunner;
import hudson.plugins.selenium.process.SeleniumRunOptions;

import java.io.Serializable;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public abstract class SeleniumNodeConfiguration extends SeleniumJarRunner implements Describable<SeleniumNodeConfiguration>, Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -6869016011243891909L;

	private String displayName = null;

    private String display;

    /**
     * @param display
     */
    public SeleniumNodeConfiguration(String display) {
        this.display = display;
    }

    public ConfigurationDescriptor getDescriptor() {
        return (ConfigurationDescriptor) Hudson.getInstance().getDescriptor(getClass());
    }

    public static DescriptorExtensionList<SeleniumNodeConfiguration, ConfigurationDescriptor> all() {
        return Hudson.getInstance().<SeleniumNodeConfiguration, ConfigurationDescriptor> getDescriptorList(SeleniumNodeConfiguration.class);
    }

    public static DescriptorExtensionList<SeleniumNodeConfiguration, ConfigurationDescriptor> allExcept(Node current) {
        return Hudson.getInstance().<SeleniumNodeConfiguration, ConfigurationDescriptor> getDescriptorList(SeleniumNodeConfiguration.class);
    }

    public SeleniumRunOptions initOptions(Computer c) {
        SeleniumRunOptions opts = new SeleniumRunOptions();

        opts.setEnvVar("DISPLAY", display);

        return opts;
    }

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

    @Exported
    public String getDisplay() {
        return display;
    }

    public String getIcon() {
        return "/images/24x24/gear.png";
    }

    public String getIconAltText() {
        return getDescriptor().getDisplayName();
    }

}
