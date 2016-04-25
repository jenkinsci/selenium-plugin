package hudson.plugins.selenium;

import antlr.ANTLRException;
import hudson.model.Label;
import hudson.model.Node;
import hudson.remoting.Channel;
import jenkins.model.Jenkins;
import jenkins.security.MasterToSlaveCallable;
import org.apache.commons.lang.StringUtils;
import org.openqa.grid.internal.utils.CapabilityMatcher;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        LOGGER.log(Level.INFO, "CURRENT : " + currentCapability.toString());
        LOGGER.log(Level.INFO, "REQUEST : " + requestedCapability.toString());

        if (!base.matches(currentCapability, requestedCapability)) {
            return false;
        }

        Object label = requestedCapability.get(LABEL);
        Object reqNode = requestedCapability.get(NODE_NAME);
        String nodeName = (String) currentCapability.get(NODE_NAME);

        if (label == null && reqNode == null) {
            return true;
        }

        LOGGER.log(Level.INFO, "NODE : " + reqNode + " - " + nodeName);

        boolean nodeMatch = false;

        if (reqNode != null && nodeName != null) {
            LOGGER.log(Level.INFO, "BOTH NOT NULL");
            nodeMatch = nodeName.equals(reqNode);
        } else nodeMatch = reqNode == null;

        if (label == null) {
            return nodeMatch;
        }

        String labelExpr = label.toString();
        if (labelExpr.trim().length() == 0) {
            LOGGER.log(Level.INFO, "returning node match");
            return nodeMatch;    // treat "" as null
        }

        try {
            return nodeMatch && master.call(new LabelMatcherCallable(nodeName, labelExpr));
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
     * Can be used as a capability when WebDriver requests a node from Grid. The value is a boolean expression over labels to select the desired node
     * to run the test.
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
    private static class LabelMatcherCallable extends MasterToSlaveCallable<Boolean, ANTLRException> {

        private final String nodeName;
        private final String labelExpr;

        public LabelMatcherCallable(String nodeName, String labelExpr) {
            this.nodeName = nodeName.equals(MASTER_NAME) ? "" : nodeName;
            this.labelExpr = labelExpr;
        }

        public Boolean call() throws ANTLRException {
            Node n = Jenkins.getInstance().getNode(nodeName);
            if (n == null)
                return false;
            return Label.parseExpression(labelExpr).matches(n);
        }

        private static final long serialVersionUID = 1L;
    }

    public static void enhanceCapabilities(DesiredCapabilities capabilities, String node) {
        String nodeName = StringUtils.isEmpty(node) ? JenkinsCapabilityMatcher.MASTER_NAME : node;
        capabilities.setCapability(JenkinsCapabilityMatcher.NODE_NAME, nodeName);
    }

    private static final Logger LOGGER = Logger.getLogger(JenkinsCapabilityMatcher.class.getName());
}
