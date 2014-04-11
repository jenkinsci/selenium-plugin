/**
 * 
 */
package hudson.plugins.selenium.process;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Richard Lavoie
 * 
 */
public class ProcessArgument implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 176693180466734322L;
    private String value;

    public ProcessArgument(String argumentValue, boolean quote) {
        // not yet implemented
        throw new UnsupportedOperationException();
    }

    public ProcessArgument(String argumentValue) {
        this.value = argumentValue;
    }

    public String toString() {
        return value;
    }

    /**
     * @return
     */
    public Collection<String> toArgumentsList() {
        return Collections.singletonList(value);
    }

}
