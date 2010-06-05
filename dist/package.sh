#!/bin/bash -ex

# incoming version of Selenium Grid
version=1.0.6
# outgoing version of the packages in Maven
distVersion=1.0.6-hudson-1

rm -rf work || true
work=$PWD/work

# download and extract
for type in bin; do
  [ -e selenium-grid-$version-$type.zip ] || wget -O selenium-grid-$version-$type.zip http://release.seleniumhq.org/selenium-grid/selenium-grid-$version-$type.zip
  mkdir -p work/$type
  unzip -q selenium-grid-$version-$type.zip -d work/$type
done

# extract the source tree
(cd src; git archive $version) | (mkdir -p work/src; cd work/src; tar xf -)

mkdir -p work/src
ln -s ../../../../selenium-grid/hub work/src/hub
ln -s ../../../../selenium-grid/remote-control work/src/remote-control
ln -s ../../../../selenium-grid/infrastructure work/src/infrastructure

# remove unused files
pushd work/bin/*
  rm -rf doc examples vendor/testng*.jar lib/*-demo-*.jar sample-scripts
  pushd lib
    # selenium-grid-remote-contorl-standalone-1.0.3 contains incompatible Jetty that causes http://clearspace.openqa.org/thread/17117
    # with vendor/selenium-server.jar, so use the standalone version
    rm $(find . -name '*.jar' | grep -v standalone | grep -v remote-control)
    rm selenium-grid-remote-control-standalone-*.jar
    # instead we need commons-httpclient
    cp ~/.m2/repository/commons-httpclient/commons-httpclient/3.1/commons-httpclient-3.1.jar .
    cp ~/.m2/repository/log4j/log4j/1.2.14/log4j-1.2.14.jar .
  popd
  tar czf ../selenium-grid-$version-min.tgz .
popd

for dir in hub/src/main/java remote-control/src/main/java infrastructure/core/src/main infrastructure/webserver/src/main/java; do
  pushd work/src/$dir
    zip -qu $work/sources.jar -r .
  popd
done

cp *.pom work
pushd work
  perl -pi -e "s/\@VERSION\@/${version}/g" *.pom
  perl -pi -e "s/\@DIST_VERSION\@/${distVersion}/g" *.pom
  for pom in *.pom; do
    mvn -f $pom $1
  done
popd

