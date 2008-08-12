RC eventually launches SeleniumServer. So far I haven't spotted anything
that requires one server per VM. Can I run it that way?

The following Jetty dependency causes sealing violation, because Jetty jars
are all sealed. This probably means I can't launch Selenium inside a webapp.

    [INFO] +- org.openqa.selenium.server:selenium-server:jar:1.0-beta-1:compile
    [INFO] |  +- org.openqa.selenium.server:selenium-server-coreless:jar:1.0-beta-1:compile
    [INFO] |  |  +- ant:ant:jar:1.6.5:compile
    [INFO] |  |  +- jetty:org.mortbay.jetty:jar:5.1.10:compile
    [INFO] |  |  +- commons-logging:commons-logging:jar:1.1:compile
    [INFO] |  |  \- bouncycastle:bcprov-jdk15:jar:135:compile
    [INFO] |  \- org.openqa.selenium.core:selenium-core:jar:1.0-beta-1:compile

Just for an experiment, I excluded 5.1.10 and I got NoClassDefError of HttpHandler.

    Exception in thread "Selenium Thread" java.lang.NoClassDefFoundError: org/mortbay/http/HttpHandler
            at com.thoughtworks.selenium.grid.remotecontrol.SelfRegisteringRemoteControl.launch(Unknown Source)
            at hudson.plugins.selenium.ComputerListenerImpl$SeleniumThread.run(ComputerListenerImpl.java:57)
    Caused by: java.lang.ClassNotFoundException: org.mortbay.http.HttpHandler
            at java.net.URLClassLoader$1.run(URLClassLoader.java:200)
            at java.security.AccessController.doPrivileged(Native Method)
            at java.net.URLClassLoader.findClass(URLClassLoader.java:188)
            at java.lang.ClassLoader.loadClass(ClassLoader.java:307)
            at java.lang.ClassLoader.loadClass(ClassLoader.java:252)
            at java.lang.ClassLoader.loadClassInternal(ClassLoader.java:320)
            ... 2 more

Interestingly this only happens when I launch Selenium RC.
That means I have to launch RC on a separate JVM.
