package com.miotech.kun.metadata.databuilder.extract.impl.elasticsearch;

import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.databuilder.client.ElasticSearchClient;
import com.miotech.kun.metadata.databuilder.constant.DatasetExistenceJudgeMode;
import com.miotech.kun.metadata.databuilder.extract.schema.DatasetExistenceExtractor;
import com.miotech.kun.metadata.databuilder.model.DataSource;
import com.miotech.kun.metadata.databuilder.model.ElasticSearchDataSource;

public class ElasticsearchExistenceExtractor implements DatasetExistenceExtractor {
    
    @Override
    public boolean judgeExistence(Dataset dataset, DataSource dataSource, DatasetExistenceJudgeMode judgeMode) {
        ElasticSearchClient elasticSearchClient = null;
        try {
            elasticSearchClient = new ElasticSearchClient((ElasticSearchDataSource) dataSource);
            return elasticSearchClient.judgeIndexExistence(dataset.getName());
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        } finally {
            if (elasticSearchClient != null) {
                elasticSearchClient.close();
            }
        }
    }
    
}
