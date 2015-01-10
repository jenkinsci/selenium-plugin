package hudson.plugins.selenium.callables;

import hudson.remoting.Callable;

import java.net.ServerSocket;

import jenkins.security.MasterToSlaveCallable;

public class RetrieveAvailablePort extends MasterToSlaveCallable<Integer, Exception> {

    /**
	 * 
	 */
    private static final long serialVersionUID = 8030109206143148731L;

    private int port;

    public RetrieveAvailablePort(int port) {
        this.port = port;
    }

    public Integer call() throws Exception {

        // this is potentially unsafe way to figure out a free port number, but it's far easier
        // than patching Selenium
        ServerSocket ss = new ServerSocket(port);
        int newport = ss.getLocalPort();
        ss.close();
        return newport;

    }

}
