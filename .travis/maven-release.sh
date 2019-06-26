#!/usr/bin/env bash

set -e

print "Ensuring that pom  matches $TRAVIS_TAG"
mvn org.codehaus.mojo:versions-maven-plugin:set -DnewVersion=$TRAVIS_TAG

print "Uploading to oss repo "
mvn deploy --settings .travis/settings.xml -DskipTests=true --batch-mode --update-snapshots