package com.miotech.kun.metadata.extract.impl;

import com.miotech.kun.metadata.extract.Extractor;
import com.miotech.kun.metadata.models.Table;

import java.util.List;

public abstract class JDBCExtractor implements Extractor {
    @Override
    public List<Table> extract() {
        return null;
    }
}
