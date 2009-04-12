package hudson.plugins.selenium;

import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.Extension;
import hudson.Util;
import hudson.model.Node;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.util.Collections;
import java.util.List;
import java.util.Arrays;

import net.sf.json.JSONObject;

/**
 * @author Kohsuke Kawaguchi
 */
public class SeleniumEnvironmentProperty extends NodeProperty<Node> {
    private String environments;

    @DataBoundConstructor
    public SeleniumEnvironmentProperty(String environments) {
        this.environments = Util.fixEmptyAndTrim(environments);
    }

    public String getEnvironments() {
        return environments;
    }

    public List<String> getEnvironmentList() {
        if(environments==null)  return Collections.emptyList();
        return Arrays.asList(environments.split("\\s*,\\s*"));
    }

    @Extension
    public static final class DescriptorImpl extends NodePropertyDescriptor {
        public String getDisplayName() {
            return "Start Selenium RCs on this node";
        }
    }
}
