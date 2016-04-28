/**
 *
 */
package hudson.plugins.selenium.callables.hub;

import hudson.plugins.selenium.HubHolder;
import jenkins.security.MasterToSlaveCallable;

import java.util.logging.Logger;

/**
 * @author Richard Lavoie
 *
 */
public class StopHubCallable extends MasterToSlaveCallable<Void, Exception> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8923286239409381397L;
	
	private static final Logger LOGGER = Logger.getLogger(StopHubCallable.class.getName());
    /*
     * (non-Javadoc)
     * @see hudson.remoting.Callable#call()
     */
    public Void call() throws Exception {
	    if (HubHolder.getHub() == null){
		    LOGGER.warning("Hub is not running. Nothing to stop.");
		    return null;
	    }

        HubHolder.getHub().getRegistry().stop();
        HubHolder.getHub().stop();
        HubHolder.setHub(null);
        return null;
    }

}
