package hudson.plugins.seleniumaes;

import hudson.model.AbstractBuild;
import hudson.model.Action;

import java.util.ArrayList;

/**
 * @author onozaty
 */
public class SeleniumAutoExecAction implements Action {

    /** build */
    private AbstractBuild<?, ?> build;

    /** result */
    private ArrayList<SeleniumAutoExecResult> resultList = new ArrayList<SeleniumAutoExecResult>();

    /**
     * @param build 
     */
    public SeleniumAutoExecAction(AbstractBuild<?, ?> build) {
        super();
        this.build = build;
    }

    /**
     * @see hudson.model.Action#getDisplayName()
     */
    public String getDisplayName() {
        return "Selenium AES Results";
    }

    /**
     * @see hudson.model.Action#getIconFileName()
     */
    public String getIconFileName() {
        return "clipboard.gif";
    }

    /**
     * @see hudson.model.Action#getUrlName()
     */
    public String getUrlName() {
        return "seleniumaes";
    }

    /**
     * @return build
     */
    public AbstractBuild<?, ?> getBuild() {
        return build;
    }

    /**
     * @return resultList
     */
    public ArrayList<SeleniumAutoExecResult> getResultList() {
        return resultList;
    }

}
