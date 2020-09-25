#!/usr/bin/env sh
dockerize -wait tcp://${DB_PG_IP}:${DB_PG_PORT} -wait-retry-interval 1s -timeout 60s
java ${JVM_OPTS} -jar /server/target/kun-data-dashboard.jar
