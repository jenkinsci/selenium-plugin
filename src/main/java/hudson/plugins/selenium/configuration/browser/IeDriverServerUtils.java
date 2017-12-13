/**
 *
 */
package hudson.plugins.selenium.configuration.browser;

import hudson.model.Computer;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;

/**
 * @author Richard Lavoie
 * 
 */
public final class IeDriverServerUtils {

    private IeDriverServerUtils() {
    }

    public static String uploadIEDriverIfNecessary(Computer computer, String serverBinary, boolean forbid64bitDriver) {
        if (StringUtils.isBlank(serverBinary)) {
            String driverName;
            NodeUtils.OsType nodeOs = NodeUtils.getNodeOS(computer);
            switch (nodeOs) {
                case WINDOWS_64:
                    if (forbid64bitDriver) {
                        driverName = "IEDriverServer_64.exe";
                        break;
                    }
                case WINDOWS_32:
                    driverName = "IEDriverServer_32.exe";
                    break;
                default:
                    return serverBinary;
            }
            URL url = IeDriverServerUtils.class.getClassLoader().getResource(driverName);
            return NodeUtils.uploadFileToNode(computer, url, "IEDriverServer.exe");
        } else {
            return serverBinary;
        }
    }
}
