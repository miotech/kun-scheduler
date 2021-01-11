package com.miotech.kun.metadata.databuilder.client;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.glue.AWSGlue;
import com.amazonaws.services.glue.AWSGlueClientBuilder;
import com.amazonaws.services.glue.model.PropertyPredicate;
import com.amazonaws.services.glue.model.SearchTablesRequest;
import com.amazonaws.services.glue.model.SearchTablesResult;
import com.amazonaws.services.glue.model.Table;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miotech.kun.metadata.databuilder.model.AWSDataSource;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class GlueClient {

    private static Map<String, Table> tables = Maps.newHashMap();
    private static AtomicBoolean initedCache = new AtomicBoolean(false);

    private GlueClient() {
    }

    public static AWSGlue getAWSGlue(String accessKey, String secretKey, String region) {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
        return AWSGlueClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion(Regions.fromName(region)).build();
    }

    public static Table searchTable(AWSDataSource awsDataSource, String targetDatabase, String targetTable) {
        AWSGlue awsGlue = getAWSGlue(awsDataSource.getGlueAccessKey(), awsDataSource.getGlueSecretKey(), awsDataSource.getGlueRegion());
        String nextToken = null;

        do {
            SearchTablesRequest searchTablesRequest = new SearchTablesRequest();
            searchTablesRequest.withNextToken(nextToken);

            List<PropertyPredicate> filters = Lists.newArrayList();

            PropertyPredicate databaseNameFilter = new PropertyPredicate();
            databaseNameFilter.withKey("databaseName");
            databaseNameFilter.withValue(targetDatabase);
            filters.add(databaseNameFilter);

            PropertyPredicate tableNameFilter = new PropertyPredicate();
            tableNameFilter.withKey("name");
            tableNameFilter.withValue(targetTable);
            filters.add(tableNameFilter);

            searchTablesRequest.setFilters(filters);
            SearchTablesResult searchTablesResult = awsGlue.searchTables(searchTablesRequest);
            for (Table table : searchTablesResult.getTableList()) {
                if (targetDatabase.equals(table.getDatabaseName()) && targetTable.equals(table.getName())) {
                    return table;
                }
            }

            nextToken = searchTablesResult.getNextToken();
        } while (StringUtils.isNotBlank(nextToken));

        return null;
    }

    public static Table searchTableWithSnapshot(AWSDataSource awsDataSource, String targetDatabase, String targetTable) {
        if (initedCache.compareAndSet(false, true)) {
            initCache(awsDataSource);
        }

        return tables.get(targetDatabase + "." + targetTable);
    }

    private static void initCache(AWSDataSource awsDataSource) {
        AWSGlue awsGlue = getAWSGlue(awsDataSource.getGlueAccessKey(), awsDataSource.getGlueSecretKey(), awsDataSource.getGlueRegion());
        String nextToken = null;

        do {
            SearchTablesResult searchTablesResult = awsGlue.searchTables(new SearchTablesRequest().withNextToken(nextToken));

            List<Table> tableList = searchTablesResult.getTableList();
            for (Table table : tableList) {
                tables.put(table.getDatabaseName() + "." + table.getName(), table);
            }

            nextToken = searchTablesResult.getNextToken();
        } while (StringUtils.isNotBlank(nextToken));
    }

}
