package com.miotech.kun.metadata.databuilder.extract.impl.hive;

import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.databuilder.client.MetaStoreClient;
import com.miotech.kun.metadata.databuilder.constant.DatasetExistenceJudgeMode;
import com.miotech.kun.metadata.databuilder.extract.schema.DatasetExistenceExtractor;
import com.miotech.kun.metadata.databuilder.model.DataSource;
import com.miotech.kun.metadata.databuilder.model.HiveDataSource;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.Table;

public class HiveExistenceExtractor implements DatasetExistenceExtractor {

    @Override
    public boolean judgeExistence(Dataset dataset, DataSource dataSource, DatasetExistenceJudgeMode judgeMode) {
        HiveDataSource hiveDataSource = (HiveDataSource) dataSource;
        HiveMetaStoreClient client = null;

        try {
            client = MetaStoreClient.getClient(hiveDataSource.getMetaStoreUris());

            Table table = client.getTable(dataset.getDatabaseName(), dataset.getName());
            return table != null;
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        } finally {
            MetaStoreClient.close(client);
        }
    }

}
