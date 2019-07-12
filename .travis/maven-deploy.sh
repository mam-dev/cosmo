#!/usr/bin/env bash

set -e

echo "Setting new version for release to $TRAVIS_TAG"
mvn org.codehaus.mojo:versions-maven-plugin:set -DnewVersion=$TRAVIS_TAG

echo "Start deploying to https://oss.sonatype.org/#stagingRepositories"
mvn clean deploy --settings .travis/settings.xml -DskipTests=true --batch-mode
