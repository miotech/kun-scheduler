package com.miotech.kun.metadata.databuilder.load;

import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.core.model.DatasetField;
import com.miotech.kun.metadata.databuilder.model.LoadSchemaResult;

import java.util.List;

public interface Loader {

    LoadSchemaResult loadSchema(Long gid, List<DatasetField> fields);

    LoadSchemaResult loadSchema(Dataset dataset);

    void loadStatistics(Long snapshotId, Dataset dataset);
}
