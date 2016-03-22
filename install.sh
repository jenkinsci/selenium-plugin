version=2.53.0
#cmd=install:install-file
cmd=deploy:deploy-file
mvn $cmd -Dfile=$HOME/Downloads/selenium-server-standalone-${version}.jar \
    -DgroupId=org.seleniumhq.selenium -DartifactId=selenium-java -Dversion=${version} -Dpackaging=jar \
    -DrepositoryId=maven.jenkins-ci.org -Durl=http://maven.jenkins-ci.org:8081/content/repositories/releases 
