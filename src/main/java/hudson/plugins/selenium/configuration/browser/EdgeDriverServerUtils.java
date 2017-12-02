package hudson.plugins.selenium.configuration.browser;

import hudson.model.Computer;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;

public class EdgeDriverServerUtils {

    private EdgeDriverServerUtils() {
    }

    private static final String EDGE_DRIVER_NAME = "MicrosoftWebDriver.exe";

    public static String uploadEdgeDriverIfNecessary(Computer computer, String serverBinary) {
        if (StringUtils.isBlank(serverBinary)) {
            String serverPath;
            NodeUtils.OsType nodeOs = NodeUtils.getNodeOS(computer);
            switch (nodeOs) {
                case WINDOWS_64:
                case WINDOWS_32:
                    URL url = IeDriverServerUtils.class.getClassLoader().getResource(EDGE_DRIVER_NAME);
                    serverPath = NodeUtils.uploadFileToNode(computer, url, EDGE_DRIVER_NAME);
                    break;
                default:
                    return serverBinary;
            }
            return serverPath;
        }
        return serverBinary;
    }
}
