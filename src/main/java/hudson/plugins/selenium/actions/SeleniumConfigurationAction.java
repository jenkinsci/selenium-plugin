package hudson.plugins.selenium.actions;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.model.AutoCompletionCandidates;
import hudson.model.Describable;
import hudson.model.ManagementLink;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Label;
import hudson.model.Node;
import hudson.model.labels.LabelAtom;
import hudson.plugins.selenium.PluginImpl;
import hudson.plugins.selenium.configuration.ConfigurationDescriptor;
import hudson.plugins.selenium.configuration.SeleniumNodeConfiguration;
import hudson.plugins.selenium.configuration.global.SeleniumGlobalConfiguration;
import hudson.plugins.selenium.configuration.global.matcher.SeleniumConfigurationMatcher;
import hudson.plugins.selenium.configuration.global.matcher.SeleniumConfigurationMatcher.MatcherDescriptor;
import hudson.security.Permission;

import java.io.IOException;
import java.util.List;

import jenkins.model.Jenkins;

import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import antlr.ANTLRException;

@Extension
public class SeleniumConfigurationAction extends ManagementLink implements Describable<SeleniumConfigurationAction> {

	public String getIconFileName() {
		return "/plugin/selenium/24x24/selenium.png";
	}

	public String getDisplayName() {
		return "Selenium configurations";
	}

	public String getUrlName() {
		return "selenium-node-config";
	}
	
	public String getDescription() {
		return "Selenium node configurations";
	}

    public List<SeleniumGlobalConfiguration> get_all() {
        return PluginImpl.getPlugin().getGlobalConfigurations();
    }
	
	
	public PluginImpl getPlugin() {
		return Jenkins.getInstance().getPlugin(PluginImpl.class);
	}
	
    /**
     * Gets the descriptor.
     * @return descriptor.
     */
    public Descriptor<SeleniumConfigurationAction> getDescriptor() {
        return Jenkins.getInstance().getDescriptorByType(DescriptorImpl.class);
    }
	
    /**
     * Descriptor is only used for auto completion.
     */
    @Extension
    public static final class DescriptorImpl extends Descriptor<SeleniumConfigurationAction> {
        
    	@Override
        public String getDisplayName() {
            return null; //Not used.
        }

        /**
         * Returns a list of auto completion candidates.
         * Used in the "copy existing slave"-textbox at the slave creation page.
         * @param value to search for
         * @return candidates
         */
        public AutoCompletionCandidates doAutoCompleteNames(@QueryParameter String value) {
            AutoCompletionCandidates candidates = new AutoCompletionCandidates();
            List<Node> masterNodeList = Hudson.getInstance().getNodes();
            for (Node node : masterNodeList) {
            	try {
					for (LabelAtom atom : Label.parseExpression(node.getLabelString()).listAtoms()) {
						candidates.add(atom.getName());
					}
				} catch (ANTLRException e) {
					// invalid expression, skipped
				}
            }
            return candidates;
        }
    }


    /**
     * Redirects to the add slaves-wizard, also setting usermode to add.
     * @param req StaplerRequest
     * @param rsp StaplerResponse to redirect with
     * @throws IOException if redirection goes wrong
     */
    public void doAddRedirect(StaplerRequest req, StaplerResponse rsp) throws IOException {
        Hudson.getInstance().checkPermission(getRequiredPermission());
        rsp.sendRedirect2("add");
    }
    
    /**
     * Redirects to the add slaves-wizard, also setting usermode to add.
     * @param req StaplerRequest
     * @param rsp StaplerResponse to redirect with
     * @throws IOException if redirection goes wrong
     */
    public void doCreate(StaplerRequest req, StaplerResponse rsp, @QueryParameter String name) throws Exception {
        Hudson.getInstance().checkPermission(getRequiredPermission());
        SeleniumGlobalConfiguration conf = req.bindJSON(SeleniumGlobalConfiguration.class, req.getSubmittedForm());
        PluginImpl.getPlugin().getGlobalConfigurations().add(conf);
        PluginImpl.getPlugin().save();
        rsp.sendRedirect2(".");
    }
    

	public Permission getRequiredPermission() {
		return Hudson.ADMINISTER;
	}
    
	public DescriptorExtensionList<SeleniumNodeConfiguration, ConfigurationDescriptor> getConfigTypes() {
		return SeleniumNodeConfiguration.all();
	}

	public DescriptorExtensionList<SeleniumConfigurationMatcher, MatcherDescriptor> getMatcherTypes() {
		return SeleniumConfigurationMatcher.all();
	}
	
}
