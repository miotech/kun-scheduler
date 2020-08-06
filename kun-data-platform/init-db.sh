#!/bin/bash
set -e

    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
CREATE USER docker with password 'docker';
CREATE DATABASE kun;
GRANT ALL PRIVILEGES ON DATABASE kun TO docker;
CREATE DATABASE dataplatform;
GRANT ALL PRIVILEGES ON DATABASE dataplatform TO docker;
EOSQL