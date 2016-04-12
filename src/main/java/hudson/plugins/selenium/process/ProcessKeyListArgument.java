/**
 *
 */
package hudson.plugins.selenium.process;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Richard Lavoie
 *
 */
public class ProcessKeyListArgument extends ProcessArgument {

    /**
     *
     */
    private static final long serialVersionUID = -138813439927716198L;

    private List<String> values;

    /**
     */
    public ProcessKeyListArgument(String keyArgument, String... values) {
        super(keyArgument);
        this.values = Arrays.asList(values);
    }

    public void addArgument(String value) {
        values.add(value);
    }

    public List<String> toArgumentsList() {
        List<String> rets = new ArrayList<String>(values.size() + 1);
        rets.addAll(super.toArgumentsList());
        rets.addAll(values);
        return rets;
    }

    public String toString() {
        return super.toString() + " " + StringUtils.join(values, " ");
    }
}
