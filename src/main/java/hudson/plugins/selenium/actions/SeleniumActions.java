package hudson.plugins.selenium.actions;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.TransientComputerActionFactory;
import hudson.model.Computer;
import hudson.plugins.selenium.PluginImpl;

import java.util.Collection;
import java.util.Collections;

@Extension
public class SeleniumActions extends TransientComputerActionFactory {

    @Override
    public Collection<? extends Action> createFor(Computer target) {

        // If user has admin privileges, let the user access selenium node configurations through the node
        if (PluginImpl.getPlugin().isAdmin()) {

            return Collections.singletonList(new ServiceManagementAction(target));
        } else {

            return Collections.EMPTY_LIST;
        }
    }
}
