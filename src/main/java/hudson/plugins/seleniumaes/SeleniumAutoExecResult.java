package hudson.plugins.seleniumaes;

import java.io.Serializable;
import java.util.ArrayList;

import com.enjoyxstudy.selenium.autoexec.client.RemoteControlClient;

/**
 * @author onozaty
 */
public class SeleniumAutoExecResult implements Serializable {

    /** serialVersionUID */
    private static final long serialVersionUID = 6857331953880672718L;

    /** serverUrl */
    private String serverUrl;

    /** passed */
    private boolean isPassed;

    /** passedCount */
    private int passedCount;

    /** failedCount */
    private int failedCount;

    /** total count */
    private int totalCount;

    /** startTime */
    private String startTime;

    /** endTime */
    private String endTime;

    /** suites */
    private ArrayList<Suite> suites;

    /**
     * @return serverUrl
     */
    public String getServerUrl() {
        return serverUrl;
    }

    /**
     * @param serverUrl serverUrl
     */
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    /**
     * @return isPassed
     */
    public boolean isPassed() {
        return isPassed;
    }

    /**
     * @param isPassed isPassed
     */
    public void setPassed(boolean isPassed) {
        this.isPassed = isPassed;
    }

    /**
     * @return passedCount
     */
    public int getPassedCount() {
        return passedCount;
    }

    /**
     * @param passedCount passedCount
     */
    public void setPassedCount(int passedCount) {
        this.passedCount = passedCount;
    }

    /**
     * @return failedCount
     */
    public int getFailedCount() {
        return failedCount;
    }

    /**
     * @param failedCount failedCount
     */
    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    /**
     * @return totalCount
     */
    public int getTotalCount() {
        return totalCount;
    }

    /**
     * @param totalCount totalCount
     */
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    /**
     * @return startTime
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * @param startTime startTime
     */
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    /**
     * @return endTime
     */
    public String getEndTime() {
        return endTime;
    }

    /**
     * @param endTime endTime
     */
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    /**
     * @return suites
     */
    public ArrayList<Suite> getSuites() {
        return suites;
    }

    /**
     * @param suites suites
     */
    public void setSuites(ArrayList<Suite> suites) {
        this.suites = suites;
    }

    /**
     * @author onozaty
     */
    public class Suite implements Serializable {

        /** serialVersionUID */
        private static final long serialVersionUID = -301770846827562197L;

        /** suiteName */
        private String suiteName;

        /** resultPath */
        private String resultPath;

        /** browser */
        private String browser;

        /** status */
        private String status;

        /**
         * @return suiteName
         */
        public String getSuiteName() {
            return suiteName;
        }

        /**
         * @param suiteName suiteName
         */
        public void setSuiteName(String suiteName) {
            this.suiteName = suiteName;
        }

        /**
         * @return resultPath
         */
        public String getResultPath() {
            return resultPath;
        }

        /**
         * @param resultPath resultPath
         */
        public void setResultPath(String resultPath) {
            this.resultPath = resultPath;
        }

        /**
         * @return browser
         */
        public String getBrowser() {
            return browser;
        }

        /**
         * @param browser browser
         */
        public void setBrowser(String browser) {
            this.browser = browser;
        }

        /**
         * @return status
         */
        public String getStatus() {
            return status;
        }

        /**
         * @param status status
         */
        public void setStatus(String status) {
            this.status = status;
        }

        /**
         * @return isPassed
         */
        public boolean isPassed() {
            return RemoteControlClient.PASSED.equals(status);
        }

    }
}
