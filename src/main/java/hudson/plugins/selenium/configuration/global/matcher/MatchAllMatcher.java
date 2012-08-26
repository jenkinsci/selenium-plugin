package hudson.plugins.selenium.configuration.global.matcher;

import hudson.Extension;
import hudson.model.Node;

import org.kohsuke.stapler.DataBoundConstructor;

@Extension
public class MatchAllMatcher extends SeleniumConfigurationMatcher {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2499088801959956260L;

	@DataBoundConstructor
	public MatchAllMatcher() {
		
	}
	
	public boolean match(Node node) {
        return true;
	}
		
	@Extension
	public static class DescriptorImpl extends MatcherDescriptor {

		@Override
		public String getDisplayName() {
			return "Match all nodes";
		}
	}
}
