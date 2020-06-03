package com.miotech.kun.metadata.extract.iterator;

import com.amazonaws.services.glue.AWSGlue;
import com.amazonaws.services.glue.model.SearchTablesRequest;
import com.amazonaws.services.glue.model.SearchTablesResult;
import com.amazonaws.services.glue.model.Table;
import com.google.common.collect.Lists;
import com.miotech.kun.metadata.client.GlueClient;

import java.util.Iterator;
import java.util.List;

public class GlueTableIterator implements Iterator<List<Table>> {

    private String nextToken;

    private String database;

    private String accessKey;

    private String secretKey;

    private String region;

    private boolean initFlag = true;

    public GlueTableIterator(String database, String accessKey, String secretKey, String region) {
        this.database = database;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.region = region;
    }

    @Override
    public boolean hasNext() {
        return initFlag ? true : nextToken != null;
    }

    @Override
    public List<Table> next() {
        List<Table> tables = Lists.newArrayList();
        AWSGlue awsGlueClient = GlueClient.getAWSGlue(accessKey,
                secretKey, region);
        /* Filter not available, because Comparator can only apply to time fields
           reference: https://docs.aws.amazon.com/zh_cn/glue/latest/webapi/API_SearchTables.html */
        SearchTablesRequest searchTablesRequest = new SearchTablesRequest();
        searchTablesRequest.withNextToken(nextToken);

        SearchTablesResult searchTablesResult = awsGlueClient.searchTables(searchTablesRequest);
        if (searchTablesResult == null) {
            return tables;
        }

        if (searchTablesResult.getTableList() != null) {
            for (Table table : searchTablesResult.getTableList()) {
                if (database.equals(table.getDatabaseName())) {
                    tables.add(table);
                }
            }
        }
        nextToken = searchTablesResult.getNextToken();
        initFlag = false;
        return tables;
    }
}
