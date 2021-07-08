package com.miotech.kun.metadata.databuilder.extract.tool;

import com.amazonaws.services.glue.model.Table;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.databuilder.client.GlueClient;
import com.miotech.kun.metadata.databuilder.client.MetaStoreClient;
import com.miotech.kun.metadata.databuilder.model.AWSDataSource;
import com.miotech.kun.metadata.databuilder.model.DataSource;
import com.miotech.kun.metadata.databuilder.model.HiveDataSource;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;

import java.util.NoSuchElementException;

public class MetaStoreParseUtil {

    private MetaStoreParseUtil() {
    }

    public static String parseOutputFormat(Type metaStoreType, DataSource dataSource, String databaseName, String tableName) {
        switch (metaStoreType) {
            case GLUE:
                Table table = GlueClient.searchTable((AWSDataSource) dataSource, databaseName, tableName);
                if (table == null) {
                    throw new NoSuchElementException("Table not found");
                }
                return table.getStorageDescriptor().getOutputFormat();

            case META_STORE_CLIENT:
                return "";
            default:
                throw new IllegalArgumentException("Invalid metaStoreType: " + metaStoreType.name());
        }
    }

    public static String parseLocation(Type metaStoreType, DataSource dataSource, String databaseName, String tableName) {
        switch (metaStoreType) {
            case GLUE:
                Table table = GlueClient.searchTable((AWSDataSource) dataSource, databaseName, tableName);
                if (table == null) {
                    throw new NoSuchElementException("Table not found");
                }
                return table.getStorageDescriptor().getLocation();

            case META_STORE_CLIENT:
                HiveDataSource hiveDataSource = (HiveDataSource) dataSource;
                HiveMetaStoreClient client = null;

                try {
                    client = MetaStoreClient.getClient(hiveDataSource.getMetaStoreUris());
                    org.apache.hadoop.hive.metastore.api.Table hiveTable = client.getTable(databaseName, tableName);
                    if (hiveTable == null) {
                        throw new NoSuchElementException("Table not found");
                    }

                    return hiveTable.getSd().getLocation();
                } catch (Exception e) {
                    throw ExceptionUtils.wrapIfChecked(e);
                } finally {
                    MetaStoreClient.close(client);
                }
            default:
                throw new IllegalArgumentException("Invalid metaStoreType: " + metaStoreType.name());
        }
    }

    public enum Type {
        GLUE, META_STORE_CLIENT;
    }


}
