package com.miotech.kun.metadata.databuilder.extract.iterator;

import com.amazonaws.services.glue.AWSGlue;
import com.amazonaws.services.glue.model.SearchTablesRequest;
import com.amazonaws.services.glue.model.SearchTablesResult;
import com.amazonaws.services.glue.model.Table;
import com.google.common.collect.Lists;
import com.miotech.kun.metadata.databuilder.client.GlueClient;
import io.prestosql.jdbc.$internal.guava.base.Preconditions;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class GlueTableIterator implements Iterator<Table> {

    private final String accessKey;

    private final String secretKey;

    private final String region;

    private List<Table> tables = Lists.newArrayList();

    private String nextToken;

    private int cursor;

    public GlueTableIterator(String accessKey, String secretKey, String region) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.region = region;
        this.nextToken = searchTables();
    }

    @Override
    public boolean hasNext() {
        if (StringUtils.isBlank(nextToken)) {
            return cursor != tables.size();
        }

        if (cursor < tables.size()) {
            return true;
        }

        nextToken = searchTables();
        return !tables.isEmpty();
    }

    @Override
    public Table next() {
        if (cursor >= tables.size()) {
            throw new NoSuchElementException();
        }

        Table resultTable = tables.get(cursor);
        cursor += 1;
        return resultTable;
    }

    private String searchTables() {
        AWSGlue awsGlueClient = GlueClient.getAWSGlue(accessKey, secretKey, region);
        SearchTablesResult searchTablesResult = awsGlueClient.searchTables(new SearchTablesRequest().withNextToken(nextToken));
        Preconditions.checkNotNull(searchTablesResult, "SearchTablesResult should not be null");

        fillTables(searchTablesResult);

        // reset cursor
        cursor = 0;
        return searchTablesResult.getNextToken();
    }

    private void fillTables(SearchTablesResult searchTablesResult) {
        if (CollectionUtils.isNotEmpty(searchTablesResult.getTableList())) {
            tables = searchTablesResult.getTableList();
        } else {
            tables = Lists.newArrayList();
        }
    }
}
