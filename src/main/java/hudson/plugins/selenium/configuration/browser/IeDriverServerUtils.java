/**
 * 
 */
package hudson.plugins.selenium.configuration.browser;

import hudson.Functions;
import hudson.model.Computer;
import hudson.remoting.RemoteInputStream;
import hudson.remoting.VirtualChannel;
import hudson.util.IOUtils;
import jenkins.MasterToSlaveFileCallable;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author Richard Lavoie
 * 
 */
public final class IeDriverServerUtils {

    private IeDriverServerUtils() {}

    public static String uploadIEDriverIfNecessary(Computer computer, String serverBinary) {
        String serverPath = null;
        if (StringUtils.isBlank(serverBinary)) {
            try {
                Boolean isWin64bit = computer.getNode().getRootPath().act(new MasterToSlaveFileCallable<Boolean>() {

                    private static final long serialVersionUID = -726600253548951419L;

                    public Boolean invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
                        if (!Functions.isWindows()) {
                            return null;
                        }
                        Process p = Runtime.getRuntime().exec("cmd /c if defined ProgramFiles(x86) ( exit 1 ) else ( exit 0 )");
                        int exitValue = p.waitFor();

                        return exitValue == 1;
                    }
                });

                if (isWin64bit != null) {
                    URL url = IeDriverServerUtils.class.getClassLoader().getResource("IEDriverServer_" + (isWin64bit ? "64" : "32") + ".exe");
                    final InputStream is = new RemoteInputStream(url.openStream(), RemoteInputStream.Flag.GREEDY);
                    serverPath = computer.getNode().getRootPath().act(new MasterToSlaveFileCallable<String>() {

                        private static final long serialVersionUID = 4508849758404950847L;

                        public String invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
                            File out = new File(f, "IEDriverServer.exe");
                            if (out.exists()) {
                                out.delete();
                            }
                            IOUtils.copy(is, out);
                            return out.getAbsolutePath();
                        }
                    });
                }

            } catch (Exception e) {
                serverPath = serverBinary;
            }
        } else {
            serverPath = serverBinary;
        }
        return serverPath;
    }

}
