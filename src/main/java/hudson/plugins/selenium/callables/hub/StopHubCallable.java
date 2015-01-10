/**
 *
 */
package hudson.plugins.selenium.callables.hub;

import java.util.logging.Logger;

import jenkins.security.MasterToSlaveCallable;
import hudson.plugins.selenium.HubHolder;
import hudson.remoting.Callable;

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
	    if (HubHolder.hub == null){
		    LOGGER.warning("Hub is not running. Nothing to stop.");
		    return null;
	    }

        HubHolder.hub.getRegistry().stop();
        HubHolder.hub.stop();
        HubHolder.hub = null;
        return null;
    }

}
