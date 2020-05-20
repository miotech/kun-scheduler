package com.miotech.kun.metadata.extract.impl;

import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.metadata.model.PostgresDatabase;
import com.miotech.kun.metadata.models.Table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PostgresExtractor extends JDBCExtractor {

    private PostgresDatabase postgresDatabase;

    private PostgresExtractor() {};

    public PostgresExtractor(PostgresDatabase postgresDatabase) {
        this.postgresDatabase = postgresDatabase;
    }

    @Override
    public Iterator<Dataset> extract() {
        List<Table> tables = new ArrayList<>();
        return null;
    }

}
