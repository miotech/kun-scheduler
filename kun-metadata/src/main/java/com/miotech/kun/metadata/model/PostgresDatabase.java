package com.miotech.kun.metadata.model;

import com.miotech.kun.metadata.constant.DatabaseType;

public class PostgresDatabase extends Database {

    {
        super.setDatabaseType(DatabaseType.POSTGRES);
    }

}
