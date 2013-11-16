#!/bin/bash

# Do maven release, then push the release created in the maven_repo project.
mvn release:clean release:prepare release:perform && ../maven-repo/push_release.sh && mvn clean && git push

