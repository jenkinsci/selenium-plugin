set -e
version=2.40.0

standaloneFile=/tmp/selenium-server-standalone-${version}.jar
standaloneUrl=https://selenium.googlecode.com/files/selenium-server-standalone-$version.jar

# download the selenium-server-standalone jar if not available
if [ ! -f $standaloneFile ]; then
    curl -o $standaloneFile $standaloneUrl
fi

# Faking local maven repository for selenium-server-standalone dependency.
mkdir -p lib/
mvn org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file \
    -Dfile=$standaloneFile \
    -DgroupId=org.seleniumhq.selenium \
    -DartifactId=selenium-server-standalone \
    -Dversion=$version \
    -Dpackaging=jar \
    -DlocalRepositoryPath=lib/

mvn package && echo "Done, packaged plugin to target/selenium.hpi."