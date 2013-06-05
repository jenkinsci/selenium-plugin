version=2.33.0
 

# download the selenium-server-standalone jar if not available
if [ ! -f $standaloneFile ]; then
    curl -o $standaloneFile $standaloneUrl
fi

# Faking local maven repository for selenium-server-standalone dependency.
mkdir lib/
mvn org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file \
    -Dfile=$HOME/Downloads/selenium-server-standalone-${version}.jar \
    -DgroupId=org.seleniumhq.selenium \
    -DartifactId=selenium-server-standalone \
    -Dversion=$version \
    -Dpackaging=jar \
    -DlocalRepositoryPath=lib/


mvn package && echo "Done, packaged plugin to target/selenium.hpi."
