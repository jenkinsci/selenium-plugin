/**
 * 
 */
package hudson.plugins.selenium.process;

import hudson.model.TaskListener;
import hudson.model.Computer;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * This interface specifies the operation necessary for a selenium process to run on a given computer
 * 
 * @author Richard Lavoie
 * 
 */
public interface SeleniumProcess {

    public void start(Computer computer, TaskListener listener, String configName) throws IOException, InterruptedException, ExecutionException;

    public void stop(Computer computer, String name);

}
