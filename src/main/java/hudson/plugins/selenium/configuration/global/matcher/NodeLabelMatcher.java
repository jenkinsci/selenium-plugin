package hudson.plugins.selenium.configuration.global.matcher;

import hudson.Extension;
import hudson.model.Label;
import hudson.model.Node;
import hudson.util.FormValidation;

import java.io.IOException;

import javax.servlet.ServletException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import antlr.ANTLRException;


@Extension
public class NodeLabelMatcher extends SeleniumConfigurationMatcher {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7254869163456438031L;
	
	private String labelExpr;

    public NodeLabelMatcher() {

    }

	@DataBoundConstructor
	public NodeLabelMatcher(String label) {
		this.labelExpr = label;
	}
	
	public boolean match(Node node) {
        if (node==null)    return false;
        try {
			return Label.parseExpression(labelExpr).matches(node);
		} catch (ANTLRException e) {
		}
        return false;
	}

	@Extension
	public static class DescriptorImpl extends MatcherDescriptor {

		@Override
		public String getDisplayName() {
			return "Match nodes from a label expression";
		}
		
        public FormValidation doCheckLabel(@QueryParameter String value) throws IOException, ServletException {
        	return FormValidation.validateRequired(value);
        }
	}

	public String getSummary() {
		return getDescriptor().getDisplayName() + " (Expression = '" + labelExpr + "' )";
	}

}
