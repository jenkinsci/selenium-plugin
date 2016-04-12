package hudson.plugins.selenium.configuration.global;

import hudson.DescriptorExtensionList;
import hudson.model.*;
import hudson.plugins.selenium.PluginImpl;
import hudson.plugins.selenium.configuration.ConfigurationDescriptor;
import hudson.plugins.selenium.configuration.SeleniumNodeConfiguration;
import hudson.plugins.selenium.configuration.global.matcher.SeleniumConfigurationMatcher;
import hudson.plugins.selenium.configuration.global.matcher.SeleniumConfigurationMatcher.MatcherDescriptor;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.ExecutionException;

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

    public void start(Computer computer, TaskListener listener) throws IOException, InterruptedException, ExecutionException {
        if (matcher != null && matcher.match(computer.getNode())) {
            configuration.start(computer, listener, name);
        }
    }

    public void stop(Computer computer) {
        if (matcher != null && matcher.match(computer.getNode())) {
            configuration.stop(computer, name);
        }
    }

    /**
     */
    public void remove(Computer computer) {
        configuration.remove(computer, name);
    }

    /**
     * Returns the name of the configuration
     *
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
        rsp.sendRedirect("../../configurations");
    }

    public Descriptor<SeleniumGlobalConfiguration> getDescriptor() {
        return Jenkins.getInstance().getDescriptorByType(DescriptorImpl.class);
    }

    public static final class DescriptorImpl extends Descriptor<SeleniumGlobalConfiguration> {

        @Override
        public String getDisplayName() {
            return null;
        }

        public FormValidation doCheckName(@QueryParameter String name) throws IOException, ServletException {
            return FormValidation.validateRequired(name);
        }

    }

    public void doCommitEdit(StaplerRequest req, StaplerResponse rsp) throws Exception {
        PluginImpl.getPlugin().validateAdmin();
        SeleniumGlobalConfiguration conf = req.bindJSON(SeleniumGlobalConfiguration.class, req.getSubmittedForm());
        if (null == conf.getName() || conf.getName().trim().equals("")) {
            throw new Failure("You must specify a name for the configuration");
        }

        PluginImpl.getPlugin().replaceGlobalConfigurations(name, conf);

        rsp.sendRedirect2("../../configurations");
    }

    public DescriptorExtensionList<SeleniumNodeConfiguration, ConfigurationDescriptor> getConfigTypes() {
        return SeleniumNodeConfiguration.all();
    }

    public DescriptorExtensionList<SeleniumConfigurationMatcher, MatcherDescriptor> getMatcherTypes() {
        return SeleniumConfigurationMatcher.all();
    }

}
