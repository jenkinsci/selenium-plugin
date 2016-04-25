package hudson.plugins.selenium;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.RestartListener;
import hudson.plugins.selenium.actions.ServiceManagementAction;

import java.io.IOException;

/**
 * Created by mobrockers on 24-4-2016.
 */
@Extension
public class RestartListenerImpl extends RestartListener {

    @Override
    public boolean isReadyToRestart() throws IOException, InterruptedException {

        for (Computer c : PluginImpl.getPlugin().getComputers().keySet()) {
            try {
                new ServiceManagementAction(c).doStop(null);
            } catch (Throwable e) {
            }
        }

        return true;
    }
}
