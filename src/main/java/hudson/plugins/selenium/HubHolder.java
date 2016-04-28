package hudson.plugins.selenium;

import org.openqa.grid.web.Hub;

/**
 * Used inside Hub JVM to hold the reference to the running {@link Hub}
 * 
 * @author Richard Lavoie
 */
public class HubHolder {

    private static Hub hub;

    public static Hub getHub() {
        return hub;
    }

    public static void setHub(Hub hub) {
        HubHolder.hub = hub;
    }
}
