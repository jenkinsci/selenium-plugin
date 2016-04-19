Updating htmlunit driver standalone is done in these steps:

1. Bump htmlunit driver version number in dist-server-standalone/pom.xml
2. Remove folder dist-server-standalone/local_m2
3. Run mvn install in dist-server-standalone/
4. Bump htmlunit-driver-standalone version number in pom.xml
6. Check in changes, and don't forget to also check in the updated htmlunit-driver-standalone jar in local_m2
