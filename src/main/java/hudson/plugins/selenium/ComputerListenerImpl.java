package hudson.plugins.selenium;

import com.thoughtworks.selenium.grid.remotecontrol.SelfRegisteringRemoteControl;
import hudson.model.Computer;
import hudson.model.Label;
import hudson.remoting.Callable;
import hudson.slaves.ComputerListener;

import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Logger;

/**
 * When a new slave is connected, launch a selenium RC.
 *
 * @author Kohsuke Kawaguchi
 */
public class ComputerListenerImpl extends ComputerListener implements Serializable {
    /**
     * Starts a selenium RC remotely.
     */
    public void onOnline(Computer c) {
        // we only do this when the slave has the label 'selenium'
        if(!c.getNode().getAssignedLabels().contains(new Label("selenium")))
            return;

        try {
            LOGGER.info("Launching Selenium RC on "+c.getName());
            c.getChannel().callAsync(new Callable<Void,Exception>() {
                public Void call() throws Exception {
                    SelfRegisteringRemoteControl server = new SelfRegisteringRemoteControl(
                            "http://localhost:4444","*chrome","localhost","5555");
                    server.register();
                    server.launch(new String[]{"-port","5555"});

                    return null;
                }
            });
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
        }
    }

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(ComputerListenerImpl.class.getName());
}
