package hudson.plugins.selenium.configuration.global.matcher;

import hudson.Extension;
import hudson.model.Node;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * This matcher always return false. Useful to disable a configuration but not remove it.
 * 
 * @author Richard Lavoie
 *
 */
@Extension
public class MatchNoneMatcher extends SeleniumConfigurationMatcher {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3110742193312586293L;


	@DataBoundConstructor
	public MatchNoneMatcher() {
		
	}
	
	public boolean match(Node node) {
        return false;
	}
	
	
	@Extension
	public static class DescriptorImpl extends MatcherDescriptor {

		@Override
		public String getDisplayName() {
			return "Match no nodes";
		}
	}

}
