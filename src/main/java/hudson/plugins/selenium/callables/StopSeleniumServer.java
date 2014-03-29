/**
 * 
 */
package hudson.plugins.selenium.callables;

import hudson.plugins.selenium.RemoteRunningStatus;
import hudson.remoting.Callable;

/**
 * @author Richard Lavoie
 * 
 */
public class StopSeleniumServer implements Callable<Void, Exception> {

    /**
     * Name of the configuration associated to the channel to stop.
     */
    private String name;

    public StopSeleniumServer(String config) {
        this.name = config;
    }

    /**
     * 
     */
    public Void call() throws Exception {
        RemoteRunningStatus rs = getRunningStatus();
        rs.setStatus("Stopping");
        callOnSubProcess(new RemoteStopSelenium(), null);
        rs.setStatus("Stopped");
        rs.setRunning(false);
        rs.getSeleniumChannel().close();
        return null;
    }

    private RemoteRunningStatus getRunningStatus() {
        return ((RemoteRunningStatus) PropertyUtils.getProperty(SeleniumConstants.PROPERTY_STATUS).get(name));
    }

    private <T, V extends Exception> T callOnSubProcess(Callable<T, V> call, T defaultValue) throws Exception {
        RemoteRunningStatus opt = (RemoteRunningStatus) PropertyUtils.getProperty(SeleniumConstants.PROPERTY_STATUS).get(name);
        if (opt == null)
            return defaultValue;
        return opt.getSeleniumChannel().call(call);
    }

}
