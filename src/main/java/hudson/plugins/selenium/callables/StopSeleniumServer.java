/**
 * 
 */
package hudson.plugins.selenium.callables;

import jenkins.security.MasterToSlaveCallable;
import hudson.plugins.selenium.RemoteRunningStatus;
import hudson.remoting.Callable;

/**
 * @author Richard Lavoie
 * 
 */
public class StopSeleniumServer extends MasterToSlaveCallable<String, Exception> {

    /**
     * 
     */
    private static final long serialVersionUID = 6048096509551676769L;

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
    public String call() throws Exception {
        RemoteRunningStatus rs = getRunningStatus();
        rs.setStatus("Stopping");
        String url = callOnSubProcess(new RemoteStopSelenium(), null);
        rs.setStatus("Stopped");
        rs.setRunning(false);
        rs.getSeleniumChannel().close();
        return url;
    }

    private RemoteRunningStatus getRunningStatus() {
        return (RemoteRunningStatus) PropertyUtils.getMapProperty(SeleniumConstants.PROPERTY_STATUS, name);
    }

    private <T, V extends Exception> T callOnSubProcess(Callable<T, V> call, T defaultValue) throws Exception {
        RemoteRunningStatus opt = PropertyUtils.getMapProperty(SeleniumConstants.PROPERTY_STATUS, name);
        if (opt == null)
            return defaultValue;
        return opt.getSeleniumChannel().call(call);
    }

}
