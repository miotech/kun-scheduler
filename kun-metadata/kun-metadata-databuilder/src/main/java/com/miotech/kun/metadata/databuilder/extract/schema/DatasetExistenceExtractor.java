package com.miotech.kun.metadata.databuilder.extract.schema;

import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.databuilder.constant.DatasetExistenceJudgeMode;
import com.miotech.kun.metadata.databuilder.model.DataSource;

public interface DatasetExistenceExtractor {

    boolean judgeExistence(Dataset dataset, DataSource dataSource, DatasetExistenceJudgeMode judgeMode);

}
