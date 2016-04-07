Updating selenium standalone server is done in four steps:

1. Bump selenium server standalone version number in dist-server-standalone/pom.xml
2. Remove folder dist-server-standalone/local_m2
3. Run mvn install in dist-server-standalone/
4. Bump selenium standalone server version number in pom.xml
5. Check all selenium dependencies (selenium team is working to move third-party dependencies out of the selenium package)
6. Check in changes, and don't forget to also check in the updated selenium-server-standalone jar in local_m2
