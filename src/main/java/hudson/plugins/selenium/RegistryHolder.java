package hudson.plugins.selenium;

import org.openqa.grid.internal.Registry;

/**
 * Used inside Hub JVM to hold the reference to the running {@link Registry}
 *
 * @author Kohsuke Kawaguchi
 */
public class RegistryHolder {
    public static Registry registry;
}
