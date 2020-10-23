| Plugin Information                                                                            |
|-----------------------------------------------------------------------------------------------|
| View Selenium [on the plugin site](https://plugins.jenkins.io/selenium) for more information. |

The current version of this plugin may not be safe to use. Please review
the following warnings before use:

-   [Complete lack of CSRF protection can lead to OS command
    injection](https://jenkins.io/security/advisory/2020-06-03/#SECURITY-1766)

![(warning)](docs/images/warning.svg)
This plugin requires Jenkins to run under Java 8 or later as of version
3.1.0
![(warning)](docs/images/warning.svg)  
This plugin turns your Jenkins cluster into a [Selenium3
Grid](https://github.com/seleniumhq/selenium/wiki) cluster, so that you
can utilize your heterogeneous Jenkins clusters to carry out Selenium
tests. It now has configurations to really specify the capabilities of
each node so it won't fail your tests when a node is started with
incompatible capabilities.

## Selenium Grid deployment on Jenkins cluster

This plugin sets up Selenium Grid in the following way

-   On master, Selenium Grid Hub is started on port 4444, unless
    configured otherwise in Jenkins global configurations. This is where
    all your tests should connect to.
-   For each slave, necessary binaries are copied and Selenium RCs are
    started.
-   RCs and the Selenium Grid Hub are hooked up together automatically.

Grid can also accept additional nodes launched outside Jenkins.

## Connecting to Selenium Grid

When you run selenium tests in stand-alone Selenium, you specify the
type of the browser in the constructor.

``` syntaxhighlighter-pre
WebDriver driver = new RemoteWebDriver(new URL("http://jenkins.mydomain:4444/wd/hub"), capability);
```

In addition to standard platform matching capability offered
out-of-the-box by Selenium Grid, Jenkins allows you to specify
"jenkins.label" as a capability, whose value is an expression of label
names to narrow down where to run the tests. See the following example:

``` syntaxhighlighter-pre
DesiredCapabilities capability = DesiredCapabilities.firefox();
// say you use the redhat5 label to indicate RHEL5 and the amd64 label to specify the architecture
capability.setCapability("jenkins.label","redhat5 && amd64");
// Say you want a specific node to thread your request, just specify the node name (it must be running a selenium configuration though)
capability.setCapability("jenkins.nodeName","(master)");
```

These capabilities are matched by a custom capability matcher.

### Connecting from Selenium 1 client

Selenium 1 clients can connect to this via the following syntax:

``` syntaxhighlighter-pre
new DefaultSelenium("jenkins.mydomain", 4444, "*firefox", 'http://amazon.com');
```

Due to the underlying code change in Selenium, this plugin no longer
allows Selenium1 clients to do label-based capability matching like the
previous versions. If this is important, please use [the selenium plugin
1.5](https://updates.jenkins-ci.org/download/plugins/selenium/), which
is the last version that shipped with Selenium Grid 1, which supported
the **"LABEL\[&LABEL&...\]:BROWSER"** syntax in the browser field to
select nodes via labels.

| browser string          | meaning                                                                                                                                                                                                                 |
|-------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| \*iexplore              | Pick an available slave randomly, and run IE there. Since there's no guarantee that the randomly selected slave can actually run IE, this way of specifying a browser is unreliable unless all your slaves are Windows. |
| windows:\*iexplore      | Pick an available slave that has the 'windows' label randomly, and run IE. This gives you assurance that IE will be executable                                                                                          |
| windows&32bit:\*firefox | Run Firefox on a node that has both 'windows' and '32bit' labels                                                                                                                                                        |

The "BROWSER" portion is passed as-is to the selenium RC. For valid
values, see
[this](https://github.com/SeleniumHQ/selenium/wiki/DesiredCapabilities).

## Changelog

#### Version 3.12.0 (December 22th, 2017)

-   Update selenium standalone server to 3.12.0
    ([dmitryyu](https://github.com/jenkinsci/selenium-plugin/pull/126))
-   Update htmlunit driver to 2.29.2
    ([dmitryyu](https://github.com/jenkinsci/selenium-plugin/pull/120))
-   Add null check on RemoteRunningStatus to prevent errors from
    crashing the plugin
    ([ryantrevisol](https://github.com/jenkinsci/selenium-plugin/pull/123))

#### Version 3.7.1 (December 22th, 2017)

-   Update selenium standalone server to 3.7.1
    ([dmitryyu](https://github.com/jenkinsci/selenium-plugin/pull/111))
-   Update htmlunit driver to
    2.28.2 ([dmitryyu](https://github.com/jenkinsci/selenium-plugin/pull/111))
-   Add edgedriver
    support ([dmitryyu](https://github.com/jenkinsci/selenium-plugin/pull/114))
-   Add ability to choose IEDriver
    version ([dmitryyu](https://github.com/jenkinsci/selenium-plugin/pull/113))
-   Fix use isAdmin from plugin instead of Jenkins isAdmin so that our
    custom selenium plugin admin role is checked for
    ([Michad](https://github.com/jenkinsci/selenium-plugin/pull/107))

#### Version 3.1.0 (February 26th, 2017)

-   Update selenium standalone server to 3.1.0  
    ![(warning)](docs/images/warning.svg)
    This plugin requires Jenkins to run under Java 8 as of version 3.1.0
    ![(warning)](docs/images/warning.svg)

&nbsp;

-   Add new icon for Edge nodes

#### Version 2.53.1 (June 9th, 2016)

-   Add ability to set max allowed sessions on hub and node, so more
    than 5 browsers can potentially run at the same time
-   Add MIT license

&nbsp;

-   Fix chosen loglevel not being applied to plugin loggers
-   Fix regression in configuration upgrading
-   Fix missing security allowed non-admin user to change node
    configuration through various ways

#### Version 2.53.0 (April 20th, 2016)

-   Update selenium server standalone to version 2.53.0
-   Add htmlunitdriver dependency
-   Implement new jenkins security spec MasterToSlaveCallable
-   Fix configurations not saving
-   Various UI fixes
-   Fix exception when trying to restart hub while it's not running
-   Cleanup of old code

#### Version 2.4.1 (April 12th, 2014)

-   Added missing configuration for system properties
    (webdriver.ie.driver and webdriver.chrome.driver for example) for
    the direct JSON configuration to be useful. 

#### Version 2.4 (April 11th, 2014)

-   Added a way to specify the hub host name for slaves to connect to.
-   Added a new JSON configuration type where you can directly specify a
    configuration directly in JSON.
-   Now bundling the IE server so it deploys it on windows machine
    directly for the RC configuration and WD configuration types.
-   Allows the hub to be restarted if needs be. Happens sometimes that
    the hub process goes nuts and needs a restart without having to
    restart jenkins.
-   Improved rendering of the selenium main page where it groups the
    available sessions per host instead of listing them all straight.
    Was not really nice when you have more than 3-4 selenium nodes
    connected to the hub.
-   When a node configuration is removed, it is now stopped on all the
    slaves and removed from the running configurations.
-   Various other small fixes and improvements in the code

#### Version 2.3 (Never released)

-   Had a problem with the release process and the plugin never got to
    the jenkins server.

#### Version 2.1 (Sept 4, 2012)

-   Rewrote the whole plugin to add configurations and service
    management.

#### Version 2.0 (Dec 26, 2011)

-   Substantially modified to work with Selenium 2.

#### Version 1.5 (Dec 24, 2011)

-   [pull request
    4](https://github.com/jenkinsci/selenium-plugin/pull/4) Added
    -browserSessionReuse option
-   Improved error diagnostics when the Jenkins URL isn't configured.

#### Version 1.4 (Mar 4, 2011)

-   Made the log level configurable
    ([JENKINS-5637](https://issues.jenkins-ci.org/browse/JENKINS-5637))
-   Fixed possible selection of wrong Selenium RC.
-   Upgraded to Selenium Grid 1.0.7
    ([JENKINS-6207](https://issues.jenkins-ci.org/browse/JENKINS-6207))

#### Version 1.3 (Jan 25, 2010)

-   Make sure hub was started before launching RCs
    ([JENKINS-5370](https://issues.jenkins-ci.org/browse/JENKINS-5370))
-   Supported trustAllSSLCertificates option
    ([JENKINS-5372](https://issues.jenkins-ci.org/browse/JENKINS-5372))

#### Version 1.2 (Sep 7, 2009)

-   Upgraded to Selenium Grid 1.0.4
-   Upgraded to Selenium Server 1.0.1
-   Allow nodes to be excluded from the Grid
-   Specify additional Remote Control startup options (-browserSideLog,
    -log, -debug, -firefoxProfileTemplate)

#### Version 1.0 (Apr 26, 2009)

-   First version
