/**
 *
 */
package hudson.plugins.selenium.process;

import hudson.FilePath;
import hudson.Launcher.LocalLauncher;
import hudson.Proc;
import hudson.model.Computer;
import hudson.model.TaskListener;
import hudson.remoting.*;
import hudson.slaves.Channels;
import hudson.util.ClasspathBuilder;
import hudson.util.JVMBuilder;
import jenkins.model.Jenkins;
import org.openqa.grid.selenium.GridLauncher;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Richard Lavoie
 *
 */
public final class SeleniumProcessUtils {

	private static final Logger LOGGER = Logger.getLogger(SeleniumProcessUtils.class.getName());

    /**
     * Locate the stand-alone server jar from the classpath. Only works on the master.
     */
    public static File findStandAloneServerJar() throws IOException {
        return Which.jarFile(GridLauncher.class);
    }

    /**
     * Locate the htmlunit driver jar from the classpath. Only works on the master.
     */
    public static File findHtmlUnitDriverJar() throws IOException {
        return Which.jarFile(HtmlUnitDriver.class);
    }

    /**
     * Launches Hub in a separate JVM.
     *
     */
    public static Channel createSeleniumGridVM(TaskListener listener) throws IOException {
        JVMBuilder vmb = new JVMBuilder();
        vmb.systemProperties(null);
        return Channels.newJVM("Selenium Grid", listener, vmb, new FilePath(Jenkins.getInstance().getRootDir()),
                new ClasspathBuilder().add(findStandAloneServerJar()).add(findHtmlUnitDriverJar()));
    }

    /**
     * Launches RC in a separate JVM.
     *
     * @param standaloneServerJar
     *            The jar file of the grid to launch.
     */
    static public Channel createSeleniumRCVM(File standaloneServerJar, File htmlUnitDriverJar, TaskListener listener, Map<String, String> properties,
            Map<String, String> envVariables) throws IOException, InterruptedException {

        String displayName = "Selenium RC";

        ClasspathBuilder classpath = new ClasspathBuilder().add(standaloneServerJar);
        // add htmlunit to classpath
        classpath.add(htmlUnitDriverJar);

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

        return createChannel("Channel to " + displayName, Computer.threadPoolForRemoting, new BufferedInputStream(SocketChannelStream.in(s)),
                new BufferedOutputStream(SocketChannelStream.out(s)), null, p);
    }

    /**
     * This is a copy of Channels.forProcess without the plugin augmenters for the channel. This is the culprit cause
     * of slaves failing because when we create the selenium process into a 3rd level of slave, it didn't have access to
     * Jenkins object graph.
     *
     * @param name Channel name
     * @param execService Executor service
     * @param in Input stream to redirect
     * @param out Output stream to redirect
     * @param header channel headers
     * @param proc Proc that handled the connection
     * @throws IOException
     */
    private static Channel createChannel(String name, ExecutorService execService, InputStream in, OutputStream out, OutputStream header, final Proc proc) throws IOException {
    	ChannelBuilder cb = new ChannelBuilder(name,execService) {
            @Override
            public Channel build(CommandTransport transport) throws IOException {
                return new Channel(this,transport) {
                    /**
                     * Kill the process when the channel is severed.
                     */
                    @Override
                    public synchronized void terminate(IOException e) {
                        super.terminate(e);
                        try {
                            proc.kill();
                        } catch (IOException x) {
                            // we are already in the error recovery mode, so just record it and move on
                            LOGGER.log(Level.INFO, "Failed to terminate the severed connection",x);
                        } catch (InterruptedException x) {
                            // process the interrupt later
                            Thread.currentThread().interrupt();
                        }
                    }

                    @Override
                    public synchronized void join() throws InterruptedException {
                        super.join();
                        // wait for the child process to complete, too
                        try {
                            proc.join();
                        } catch (IOException e) {
                            throw new IOError(e);
                        }
                    }
                };
            }
        };
        cb.withHeaderStream(header);

        return cb.build(in,out);
    }
}
