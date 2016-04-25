package hudson.plugins.selenium.callables.running;

import hudson.plugins.selenium.callables.PropertyUtils;
import hudson.plugins.selenium.callables.SeleniumConstants;
import jenkins.security.MasterToSlaveCallable;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class RemoteGetConfigurations extends MasterToSlaveCallable<Set<String>, Exception> {

    /**
	 * 
	 */
    private static final long serialVersionUID = 4826936469266107568L;

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    public Set<String> call() throws Exception {
        Map map = PropertyUtils.getProperty(SeleniumConstants.PROPERTY_STATUS);
        if (map != null)
            return map.keySet();
        return Collections.emptySet();
    }

}
