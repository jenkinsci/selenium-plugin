package hudson.plugins.selenium.configuration.global;

import hudson.DescriptorExtensionList;
import hudson.model.Describable;
import hudson.model.Failure;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.plugins.selenium.PluginImpl;
import hudson.plugins.selenium.SeleniumRunOptions;
import hudson.plugins.selenium.configuration.ConfigurationDescriptor;
import hudson.plugins.selenium.configuration.SeleniumNodeConfiguration;
import hudson.plugins.selenium.configuration.global.matcher.SeleniumConfigurationMatcher;
import hudson.plugins.selenium.configuration.global.matcher.SeleniumConfigurationMatcher.MatcherDescriptor;
import hudson.util.FormValidation;

import java.io.IOException;
import java.io.Serializable;

import javax.servlet.ServletException;

import jenkins.model.Jenkins;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public class SeleniumGlobalConfiguration implements Serializable, Describable<SeleniumGlobalConfiguration> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8366478398033252973L;

	private String name;
	
	private SeleniumConfigurationMatcher matcher;
	
	private SeleniumNodeConfiguration configuration;
	
	
	@DataBoundConstructor
	public SeleniumGlobalConfiguration(String name, SeleniumConfigurationMatcher matcher, SeleniumNodeConfiguration configuration) {
		this.name = name;
		this.matcher = matcher;
		this.configuration = configuration;
	}
	
	/**
	 * This methods returns the options for a node, null if the underlying matcher doesn't match for this node.
	 * 
	 * @return Selenium startup options
	 */
	public SeleniumRunOptions getOptions(Computer c) {
		if (matcher == null) {
			return null;
		}
		if (!matcher.match(c.getNode())) {
			return null;
		}
		
		return configuration.initOptions(c);
	}
	
	/**
	 * Returns the name of the configuration
	 * @return
	 */
	@Exported
	public String getName() {
		return name;
	}
	
	public String getMatcherSummary() {
		return matcher.getSummary();
	}

	@Exported
	public SeleniumConfigurationMatcher getMatcher() {
		return matcher;
	}
	
	@Exported
	public SeleniumNodeConfiguration getConfiguration() {
		return configuration;
	}
	
	public String getDisplayName() {
		return configuration.getDescriptor().getDisplayName();
	}
	
    public void doDoDelete(StaplerRequest req, StaplerResponse rsp) throws Exception {
        Hudson.getInstance().checkPermission(Hudson.ADMINISTER);        
        PluginImpl.getPlugin().removeGlobalConfigurations(name);
        PluginImpl.getPlugin().save();
        rsp.sendRedirect("../../configurations");
    }

	public Descriptor<SeleniumGlobalConfiguration> getDescriptor() {
		return Jenkins.getInstance().getDescriptorByType(DescriptorImpl.class);
	}
	
	public static final class DescriptorImpl extends Descriptor<SeleniumGlobalConfiguration> {

		@Override
		public String getDisplayName() {
			// TODO Auto-generated method stub
			return null;
		}
		
        public FormValidation doCheckName(@QueryParameter String name) throws IOException, ServletException {
        	return FormValidation.validateRequired(name);
        }

	}

    
//    public void doEditRedirect(StaplerRequest req, StaplerResponse rsp) throws IOException {
//        Hudson.getInstance().checkPermission(getRequiredPermission());
//        String name = req.getRestOfPath().substring(1);
//        config = PluginImpl.getPlugin().getGlobalConfigurations(name);
//        //rsp.sendRedirect2("edit");
//    }
//
//    
    public void doCommitEdit(StaplerRequest req, StaplerResponse rsp) throws Exception {
        Hudson.getInstance().checkPermission(PluginImpl.getPlugin().getRequiredPermission());
        SeleniumGlobalConfiguration conf = req.bindJSON(SeleniumGlobalConfiguration.class, req.getSubmittedForm());
        if (null == conf.getName() || conf.getName().trim().equals("")) {
        	throw new Failure("You must specify a name for the configuration");
        } 
        
        SeleniumGlobalConfiguration oldConf = PluginImpl.getPlugin().getGlobalConfigurationWithName(conf.getName());
        if (oldConf != null && oldConf != this) {
        	throw new Failure("The configuration name you specified is already taken");
        }
        
        PluginImpl.getPlugin().removeGlobalConfigurations(name);
        PluginImpl.getPlugin().getGlobalConfigurations().add(conf);
        PluginImpl.getPlugin().save();
        rsp.sendRedirect2("../../configurations");
    }


	public DescriptorExtensionList<SeleniumNodeConfiguration, ConfigurationDescriptor> getConfigTypes() {
		return SeleniumNodeConfiguration.all();
	}

	public DescriptorExtensionList<SeleniumConfigurationMatcher, MatcherDescriptor> getMatcherTypes() {
		return SeleniumConfigurationMatcher.all();
	}

}
