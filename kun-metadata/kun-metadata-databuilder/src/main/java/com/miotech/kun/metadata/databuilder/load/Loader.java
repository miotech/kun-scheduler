package com.miotech.kun.metadata.databuilder.load;

import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.core.model.DatasetField;

import java.util.List;

public interface Loader {

    void load(Dataset dataset);

    void loadSchema(Long gid, List<DatasetField> fields);

    void loadSchema(Dataset dataset);

    void loadStat(Dataset dataset);
}
