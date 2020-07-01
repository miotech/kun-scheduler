package com.miotech.kun.metadata.databuilder.client;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.glue.AWSGlue;
import com.amazonaws.services.glue.AWSGlueClientBuilder;
import com.amazonaws.services.glue.model.SearchTablesRequest;
import com.amazonaws.services.glue.model.SearchTablesResult;
import com.amazonaws.services.glue.model.Table;
import com.miotech.kun.metadata.databuilder.exception.TableNotFoundException;
import com.miotech.kun.metadata.databuilder.model.AWSDataSource;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class GlueClient {

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
            SearchTablesResult searchTablesResult = awsGlue.searchTables(new SearchTablesRequest().withNextToken(nextToken));

            List<Table> tableList = searchTablesResult.getTableList();
            for (Table table : tableList) {
                if (targetDatabase.equals(table.getDatabaseName()) && targetTable.equals(table.getName())) {
                    return table;
                }
            }

            nextToken = searchTablesResult.getNextToken();
        } while (StringUtils.isNotBlank(nextToken));

        throw new TableNotFoundException(String.format("Table not found, database: {}, table: {}", targetDatabase, targetTable));
    }

}
