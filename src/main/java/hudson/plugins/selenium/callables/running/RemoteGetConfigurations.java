package hudson.plugins.selenium.callables.running;

import hudson.plugins.selenium.callables.PropertyUtils;
import hudson.plugins.selenium.callables.SeleniumConstants;
import hudson.remoting.Callable;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import jenkins.security.MasterToSlaveCallable;

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
