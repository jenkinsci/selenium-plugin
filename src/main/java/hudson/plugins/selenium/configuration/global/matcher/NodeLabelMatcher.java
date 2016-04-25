package hudson.plugins.selenium.configuration.global.matcher;

import antlr.ANTLRException;
import hudson.Extension;
import hudson.model.AutoCompletionCandidates;
import hudson.model.Label;
import hudson.model.Node;
import hudson.model.labels.LabelAtom;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.export.Exported;

import java.util.List;

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
        if (node == null)
            return false;
        try {
            return Label.parseExpression(labelExpr).matches(node);
        } catch (ANTLRException e) {
        }
        return false;
    }

    @Exported
    public String getLabel() {
        return labelExpr;
    }

    @Extension
    public static class DescriptorImpl extends MatcherDescriptor {

        @Override
        public String getDisplayName() {
            return "Match nodes from a label expression";
        }

        public FormValidation doCheckLabel(@QueryParameter String value) {
            return FormValidation.validateRequired(value);
        }

        /**
         * Returns a list of auto completion candidates.
         *
         * @return candidates
         */
        public AutoCompletionCandidates doAutoCompleteLabel() {
            AutoCompletionCandidates candidates = new AutoCompletionCandidates();
            List<Node> masterNodeList = Jenkins.getInstance().getNodes();
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

    public String getSummary() {
        return getDescriptor().getDisplayName() + " (Expression = '" + labelExpr + "' )";
    }

}
