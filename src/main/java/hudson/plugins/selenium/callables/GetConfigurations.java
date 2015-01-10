package hudson.plugins.selenium.callables;

import hudson.plugins.selenium.process.SeleniumRunOptions;

import java.util.Map;

import jenkins.security.MasterToSlaveCallable;

public class GetConfigurations extends MasterToSlaveCallable<Map<String, SeleniumRunOptions>, Exception> {

    /**
	 * 
	 */
    private static final long serialVersionUID = 8710676054398822727L;

    @SuppressWarnings( "unchecked" )
    public Map<String, SeleniumRunOptions> call() throws Exception {
        return PropertyUtils.getProperty(SeleniumConstants.PROPERTY_STATUS);
    }

}
