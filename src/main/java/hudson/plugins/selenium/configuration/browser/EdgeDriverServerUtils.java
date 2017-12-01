package hudson.plugins.selenium.configuration.browser;

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

public class EdgeDriverServerUtils {

	private EdgeDriverServerUtils() {}
	private static final String EDGE_DRIVER_NAME = "MicrosoftWebDriver.exe";

	public static String uploadEdgeDriverIfNecessary(Computer computer, String serverBinary) {

		String serverPath;
		if (StringUtils.isBlank(serverBinary)) {
			try {
					URL url = EdgeDriverServerUtils.class.getClassLoader().getResource(EDGE_DRIVER_NAME);
					final InputStream is = new RemoteInputStream(url.openStream(), RemoteInputStream.Flag.GREEDY);
					serverPath = computer.getNode().getRootPath().act(new MasterToSlaveFileCallable<String>() {

						private static final long serialVersionUID = 1252349458436321367L;

						public String invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
							File out = new File(f, EDGE_DRIVER_NAME);
							if (out.exists()) {
								out.delete();
							}
							IOUtils.copy(is, out);
							return out.getAbsolutePath();
						}
					});
			} catch (Exception e) {
				serverPath = serverBinary;
			}
		} else {
			serverPath = serverBinary;
		}
		return serverPath;
	}

}
