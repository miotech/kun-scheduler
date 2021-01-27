package com.miotech.kun.metadata.databuilder.load;

import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.core.model.DatasetField;

import java.util.List;

public interface Loader {

    void loadSchema(Long gid, List<DatasetField> fields);

    long loadSchema(Dataset dataset);

    void loadStatistics(Dataset dataset);
}
