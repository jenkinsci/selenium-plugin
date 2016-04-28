package hudson.plugins.selenium.configuration;

import hudson.DescriptorExtensionList;
import hudson.model.Computer;
import hudson.model.Describable;
import hudson.model.Node;
import hudson.plugins.selenium.process.SeleniumJarRunner;
import hudson.plugins.selenium.process.SeleniumRunOptions;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.Serializable;

@ExportedBean
public abstract class SeleniumNodeConfiguration extends SeleniumJarRunner implements Describable<SeleniumNodeConfiguration>, Serializable {

    /**
	 *
	 */
	private static final long serialVersionUID = -6869016011243891909L;

	private String displayName = null;

    private String display;

    /**
     */
    public SeleniumNodeConfiguration(String display) {
        this.display = display;
    }

    public ConfigurationDescriptor getDescriptor() {
        return (ConfigurationDescriptor) Jenkins.getInstance().getDescriptor(getClass());
    }

    public static DescriptorExtensionList<SeleniumNodeConfiguration, ConfigurationDescriptor> all() {
        return Jenkins.getInstance().getDescriptorList(SeleniumNodeConfiguration.class);
    }

    public static DescriptorExtensionList<SeleniumNodeConfiguration, ConfigurationDescriptor> allExcept(Node current) {
        return Jenkins.getInstance().getDescriptorList(SeleniumNodeConfiguration.class);
    }

    public SeleniumRunOptions initOptions(Computer c) {
        SeleniumRunOptions opts = new SeleniumRunOptions();

        opts.setEnvVar("DISPLAY", display);

        return opts;
    }

    public String getDisplayName() {
        if (displayName == null) {
            String name = getClass().getSimpleName();
            StringBuilder b = new StringBuilder(name.length());
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
