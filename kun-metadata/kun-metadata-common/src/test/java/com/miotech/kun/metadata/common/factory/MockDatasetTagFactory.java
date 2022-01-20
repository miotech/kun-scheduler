package com.miotech.kun.metadata.common.factory;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.metadata.core.model.dataset.DatasetTag;
import org.neo4j.ogm.annotation.Id;

public class MockDatasetTagFactory {

    private MockDatasetTagFactory() {
    }

    public static DatasetTag create() {
        Long id = IdGenerator.getInstance().nextId();
        Long gid = IdGenerator.getInstance().nextId();
        String tag = "tag_test";
        return new DatasetTag(id, gid, tag);
    }

}
