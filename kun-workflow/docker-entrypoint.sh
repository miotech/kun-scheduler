#!/usr/bin/env sh
SENTRY_DSN=${sentry_dsn} SENTRY_ENVIRONMENT=${env} SENTRY_RELEASE=${tag} java ${JAVA_OPTS} -jar /server/target/kun-workflow.jar -Dtag=${tag} -Denv=${env}
