#!/usr/bin/env sh
dockerize -wait http://${METADATA_HOST}:${METADATA_PORT}/health -wait-retry-interval 3s -timeout 180s
java ${JVM_OPTS} -jar /server/target/kun-data-discovery.jar
