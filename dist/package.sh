#!/bin/bash -ex
version=1.0.3

rm -rf work || true
work=$PWD/work

for type in bin src; do
  [ -e selenium-grid-$version-$type.zip ] || wget -O selenium-grid-$version-$type.zip http://release.seleniumhq.org/selenium-grid/selenium-grid-$version-$type.zip
  mkdir -p work/$type
  unzip -q selenium-grid-$version-$type.zip -d work/$type
done

pushd work/bin/*
  rm -rf doc examples vendor/testng*.jar lib/*-demo-*.jar sample-scripts
  pushd lib
    rm $(find . -name '*.jar' | grep -v standalone)
    cp $work/../log4j.jar .
  popd
  tar czf ../selenium-grid-$version-min.tgz .
popd

for type in hub remote-control; do
  pushd work/src/$type/src/main/java
    zip -q $work/$type-sources.jar -r .
  popd
done

cp *.pom work
pushd work
  perl -pi -e "s/\@VERSION\@/${version}/g" *.pom
  for pom in *.pom; do
    mvn -f $pom $1
  done
popd

