/**
 * 
 */
package hudson.plugins.selenium.callables.hub;

import hudson.plugins.selenium.HubHolder;
import hudson.remoting.Callable;

/**
 * @author Richard Lavoie
 * 
 */
public class StopHubCallable implements Callable<Void, Exception> {

    /*
     * (non-Javadoc)
     * @see hudson.remoting.Callable#call()
     */
    public Void call() throws Exception {
        HubHolder.hub.getRegistry().stop();
        HubHolder.hub.stop();
        HubHolder.hub = null;
        return null;
    }

}
