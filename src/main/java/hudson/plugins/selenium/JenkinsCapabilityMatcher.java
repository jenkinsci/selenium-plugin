package hudson.plugins.selenium;

import hudson.model.Computer;
import hudson.model.Hudson;
import hudson.model.Hudson.MasterComputer;
import hudson.model.Label;
import hudson.model.Node;
import hudson.remoting.Callable;
import hudson.remoting.Channel;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.grid.internal.utils.CapabilityMatcher;

import antlr.ANTLRException;

/**
 * {@link CapabilityMatcher} that adds "jenkins.label" support.
 *
 * @author Kohsuke Kawaguchi
 */
public class JenkinsCapabilityMatcher implements CapabilityMatcher {
    private final CapabilityMatcher base;
    private final Channel master;

    public JenkinsCapabilityMatcher(Channel master, CapabilityMatcher base) {
        this.master = master;
        this.base = base;
    }

    public boolean matches(Map<String, Object> currentCapability, Map<String, Object> requestedCapability) {
        LOGGER.log(Level.INFO, currentCapability.toString());
        LOGGER.log(Level.INFO, requestedCapability.toString());

        if (!base.matches(currentCapability,requestedCapability))
            return false;

        Object label = requestedCapability.get(LABEL);
        if (label == null)    return true;    // no additional matching required

        String labelExpr = label.toString();
        if (labelExpr.trim().length() == 0)   return true;    // treat "" as null

        String nodeName = (String)currentCapability.get(NODE_NAME);
        if (nodeName == null)
            return false;   // must have been added from elsewhere

        try {
            return master.call(new LabelMatcherCallable(nodeName, labelExpr));
        } catch (IOException e) {
            LOGGER.log(Level.INFO, "Failed to communicate with master for capability matching", e);
        } catch (ANTLRException e) {
            LOGGER.log(Level.INFO, "Invalid label expression: " + label, e);
        } catch (InterruptedException e) {
            LOGGER.log(Level.INFO, "Failed to communicate with master for capability matching", e);
        }
        return false;
    }

    /**
     * Can be used as a capability when WebDriver requests a node from Grid.
     * The value is a boolean expression over labels to select the desired node to run the test.
     */
    public static final String LABEL = "jenkins.label";
    /**
     * RC uses this to register, to designate its origin.
     */
    public static final String NODE_NAME = "jenkins.nodeName";
    
    
    /**
     * Node name of the master computer
     */
    public static final String MASTER_NAME = "(master)";

    /**
     * Checks if the given node satisfies the label expression.
     */
    private static class LabelMatcherCallable implements Callable<Boolean,ANTLRException> {
        private final String nodeName;
        private final String labelExpr;

        public LabelMatcherCallable(String nodeName, String labelExpr) {
            this.nodeName = nodeName.equals(MASTER_NAME) ? "" : nodeName;
            this.labelExpr = labelExpr;
        }

        public Boolean call() throws ANTLRException {
        	Node n = Hudson.getInstance().getNode(nodeName);
            System.out.println(n);
            if (n==null)    return false;
            return Label.parseExpression(labelExpr).matches(n);
        }

        private static final long serialVersionUID = 1L;
    }

    private static final Logger LOGGER = Logger.getLogger(JenkinsCapabilityMatcher.class.getName());
}
