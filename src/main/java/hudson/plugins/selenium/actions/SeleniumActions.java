package hudson.plugins.selenium.actions;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.TransientComputerActionFactory;
import hudson.model.Computer;

import java.util.Collection;
import java.util.Collections;

@Extension
public class SeleniumActions extends TransientComputerActionFactory {

	@Override
	public Collection<? extends Action> createFor(Computer target) {
		return Collections.singletonList(new ServiceManagementAction(target));
	}

}
