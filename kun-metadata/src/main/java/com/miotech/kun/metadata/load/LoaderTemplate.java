package com.miotech.kun.metadata.load;

import com.miotech.kun.metadata.model.Dataset;

public abstract class LoaderTemplate implements Loader {

    protected abstract void loadDatasetSchema(Dataset dataset);

    protected abstract void loadDatasetStats(Dataset dataset);

    protected abstract void loadFieldsAndStats(Dataset dataset);

    @Override
    public void load(Dataset dataset) {
        loadDatasetSchema(dataset);
        loadDatasetStats(dataset);
        loadFieldsAndStats(dataset);
    }
}
