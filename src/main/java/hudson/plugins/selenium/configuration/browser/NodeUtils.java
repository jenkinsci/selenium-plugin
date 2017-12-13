package hudson.plugins.selenium.configuration.browser;

import hudson.Functions;
import hudson.model.Computer;
import hudson.remoting.RemoteInputStream;
import hudson.remoting.VirtualChannel;
import hudson.util.IOUtils;
import jenkins.MasterToSlaveFileCallable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class NodeUtils {

    enum OsType {
        WINDOWS_32,
        WINDOWS_64,
        OTHER,
        NOT_DEFINED
    }

    static OsType getNodeOS(Computer computer) {
        try {
            return computer.getNode().getRootPath().act(new MasterToSlaveFileCallable<OsType>() {
                private static final long serialVersionUID = -726600253548951419L;

                public OsType invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
                    if (!Functions.isWindows()) {
                        return OsType.OTHER;
                    }
                    Process p = Runtime.getRuntime().exec("cmd /c if defined ProgramFiles(x86) ( exit 1 ) else ( exit 0 )");
                    int exitValue = p.waitFor();

                    return exitValue == 1 ? OsType.WINDOWS_64 : OsType.WINDOWS_32;
                }
            });
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return OsType.NOT_DEFINED;
    }


    public static String uploadFileToNode(Computer computer, URL url, String outputFileName) {
        try {
            final InputStream is = new RemoteInputStream(url.openStream(), RemoteInputStream.Flag.GREEDY);
            return computer.getNode().getRootPath().act(new MasterToSlaveFileCallable<String>() {
                private static final long serialVersionUID = 4508849758404950847L;

                public String invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
                    File out = new File(f, outputFileName);
                    if (out.exists()) {
                        out.delete();
                    }
                    IOUtils.copy(is, out);
                    return out.getAbsolutePath();
                }
            });
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
