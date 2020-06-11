package com.miotech.kun.metadata.extract.iterator;

import com.amazonaws.services.glue.AWSGlue;
import com.amazonaws.services.glue.model.SearchTablesRequest;
import com.amazonaws.services.glue.model.SearchTablesResult;
import com.amazonaws.services.glue.model.Table;
import com.google.common.collect.Lists;
import com.miotech.kun.metadata.client.GlueClient;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Iterator;
import java.util.List;

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
        Table resultTable = tables.get(cursor);
        cursor += 1;
        return resultTable;
    }

    private String searchTables() {
        AWSGlue awsGlueClient = GlueClient.getAWSGlue(accessKey, secretKey, region);
        /* Filter not available, because Comparator can only apply to time fields
           reference: https://docs.aws.amazon.com/zh_cn/glue/latest/webapi/API_SearchTables.html */
        SearchTablesResult searchTablesResult = awsGlueClient.searchTables(new SearchTablesRequest().withNextToken(nextToken));
        if (searchTablesResult == null) {
            throw new RuntimeException("SearchTablesResult should not be null");
        }

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
