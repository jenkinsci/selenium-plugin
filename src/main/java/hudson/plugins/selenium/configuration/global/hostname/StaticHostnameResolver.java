/**
 *
 */
package hudson.plugins.selenium.configuration.global.hostname;

import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Richard Lavoie
 *
 */
public class StaticHostnameResolver extends HostnameResolver {

    /**
     *
     */
    private static final long serialVersionUID = -6854399632708036684L;

    private String hostname;

	@DataBoundConstructor
    public StaticHostnameResolver(String hostname) {
        this.hostname = hostname;
    }

	public String retrieveHost() {
		return hostname;
	}

	public String getHostname() {
		return hostname;
	}

    @Extension
    public static final class StaticHostnameRetrieverDescriptor extends HostnameResolverDescriptor {

        @Override
        public String getDisplayName() {
            return "Use a fixed hostname";
        }

    }

}
