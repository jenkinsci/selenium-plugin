/**
 * 
 */
package hudson.plugins.selenium.process;

import hudson.FilePath;
import hudson.Launcher.LocalLauncher;
import hudson.Proc;
import hudson.model.TaskListener;
import hudson.model.Computer;
import hudson.model.Hudson;
import hudson.remoting.Channel;
import hudson.remoting.Launcher;
import hudson.remoting.SocketInputStream;
import hudson.remoting.SocketOutputStream;
import hudson.remoting.Which;
import hudson.slaves.Channels;
import hudson.util.ClasspathBuilder;
import hudson.util.JVMBuilder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import org.openqa.grid.selenium.GridLauncher;

/**
 * @author Richard Lavoie
 * 
 */
public final class SeleniumProcessUtils {

    /**
     * Locate the stand-alone server jar from the classpath. Only works on the master.
     */
    public static File findStandAloneServerJar() throws IOException {
        return Which.jarFile(GridLauncher.class);
    }

    /**
     * Launches Hub in a separate JVM.
     * 
     * @param rootDir
     *            The slave/master root.
     */
    public static Channel createSeleniumGridVM(TaskListener listener) throws IOException, InterruptedException {
        JVMBuilder vmb = new JVMBuilder();
        vmb.systemProperties(null);
        return Channels.newJVM("Selenium Grid", listener, vmb, new FilePath(Hudson.getInstance().getRootDir()),
                new ClasspathBuilder().add(findStandAloneServerJar()));
    }

    /**
     * Launches RC in a separate JVM.
     * 
     * @param standaloneServerJar
     *            The jar file of the grid to launch.
     */
    static public Channel createSeleniumRCVM(File standaloneServerJar, TaskListener listener, Map<String, String> properties,
            Map<String, String> envVariables) throws IOException, InterruptedException {

        String displayName = "Selenium RC";

        ClasspathBuilder classpath = new ClasspathBuilder().add(standaloneServerJar);
        JVMBuilder vmb = new JVMBuilder();
        vmb.systemProperties(properties);

        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress("localhost", 0));
        serverSocket.setSoTimeout(10000);

        // use -cp + FQCN instead of -jar since remoting.jar can be rebundled
        // (like in the case of the swarm plugin.)
        vmb.classpath().addJarOf(Channel.class);
        vmb.mainClass(Launcher.class);

        if (classpath != null)
            vmb.args().add("-cp").add(classpath);
        vmb.args().add("-connectTo", "localhost:" + serverSocket.getLocalPort());

        // TODO add XVFB options here
        Proc p = vmb.launch(new LocalLauncher(listener)).stdout(listener).envs(envVariables).start();

        Socket s = serverSocket.accept();
        serverSocket.close();

        return Channels.forProcess("Channel to " + displayName, Computer.threadPoolForRemoting, new BufferedInputStream(new SocketInputStream(s)),
                new BufferedOutputStream(new SocketOutputStream(s)), null, p);

    }
}
