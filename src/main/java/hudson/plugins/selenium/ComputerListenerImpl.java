package hudson.plugins.selenium;

import hudson.Extension;
import hudson.model.TaskListener;
import hudson.model.Computer;
import hudson.plugins.selenium.actions.ServiceManagementAction;
import hudson.slaves.ComputerListener;

import java.io.IOException;
import java.io.Serializable;

/**
 * When a new slave is connected, launch a selenium RC.
 *
 * @author Kohsuke Kawaguchi
 * @author Richard Lavoie
 */
@Extension
public class ComputerListenerImpl extends ComputerListener implements Serializable {
	
    /**
     * Starts a selenium Grid node remotely.
     */
	@Override
    public void onOnline(Computer c, final TaskListener listener) throws IOException, InterruptedException {
		PluginImpl.startSeleniumNode(c, listener, null);
    }

	
	@Override
	public void onOffline(Computer c) {
		try {
			new ServiceManagementAction(c).doStop(null);
		} catch (Throwable e) {
		}
	}

    private static final long serialVersionUID = 1L;
    
}
