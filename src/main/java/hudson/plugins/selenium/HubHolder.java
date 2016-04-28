package hudson.plugins.selenium;

import org.openqa.grid.web.Hub;

/**
 * Used inside Hub JVM to hold the reference to the running {@link Hub}
 * 
 * @author Richard Lavoie
 */
public class HubHolder {

    public static Hub hub;

    private HubHolder() {}
}
