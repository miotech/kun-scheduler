package com.miotech.kun.metadata.databuilder.extract.schema;

import com.miotech.kun.metadata.core.model.constant.DatasetExistenceJudgeMode;
import com.miotech.kun.metadata.core.model.dataset.Dataset;

public interface DatasetExistenceExtractor {

    boolean judgeExistence(Dataset dataset, DatasetExistenceJudgeMode judgeMode);

}
