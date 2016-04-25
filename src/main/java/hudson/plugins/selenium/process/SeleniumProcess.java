/**
 * 
 */
package hudson.plugins.selenium.process;

import hudson.model.Computer;
import hudson.model.TaskListener;

import java.io.IOException;

/**
 * This interface specifies the operation necessary for a selenium process to run on a given computer
 * 
 * @author Richard Lavoie
 * 
 */
public interface SeleniumProcess {

    void start(Computer computer, TaskListener listener, String configName) throws IOException, InterruptedException;

    void stop(Computer computer, String name);

}
