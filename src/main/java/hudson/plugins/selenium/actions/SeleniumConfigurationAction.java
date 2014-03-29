package hudson.plugins.selenium.actions;

import hudson.Extension;
import hudson.model.ManagementLink;

@Extension
public class SeleniumConfigurationAction extends ManagementLink {

    public String getIconFileName() {
        return "/plugin/selenium/24x24/selenium.png";
    }

    public String getDisplayName() {
        return "Selenium configurations";
    }

    public String getUrlName() {
        return "selenium/configurations";
    }

    public String getDescription() {
        return "Selenium node configurations";
    }

}
