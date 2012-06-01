package hudson.plugins.selenium;

import hudson.Extension;
import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.console.HyperlinkNote;
import hudson.model.TaskListener;
import hudson.model.Computer;
import hudson.model.Hudson;
import hudson.model.Label;
import hudson.plugins.selenium.callables.PropertyUtils;
import hudson.plugins.selenium.callables.SeleniumConstants;
import hudson.remoting.Channel;
import hudson.remoting.Future;
import hudson.remoting.VirtualChannel;
import hudson.slaves.ComputerListener;
import hudson.util.IOException2;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.util.StringUtils;

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
		PluginImpl.startSeleniumNode(c, listener);
    }


    private static final long serialVersionUID = 1L;
    
}
