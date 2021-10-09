package com.miotech.kun.metadata.common.dao;

import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.metadata.common.factory.MockDatasetSnapshotFactory;
import com.miotech.kun.metadata.common.utils.JSONUtils;
import com.miotech.kun.metadata.core.model.dataset.DatasetSnapshot;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class DatasetSnapshotDaoTest extends DatabaseTestBase {

    @Inject
    private DatasetSnapshotDao datasetSnapshotDao;

    @Test
    public void testFindByDatasetGid_withExistGid() {
        // Create dataset_snapshot
        DatasetSnapshot datasetSnapshot = MockDatasetSnapshotFactory.create();
        datasetSnapshotDao.create(datasetSnapshot);

        // Execute
        List<DatasetSnapshot> datasetSnapshots = datasetSnapshotDao.findByDatasetGid(datasetSnapshot.getDatasetGid());

        // Assert
        assertThat(datasetSnapshots.size(), is(1));
        DatasetSnapshot datasetSnapshotOfFetch = datasetSnapshots.get(0);
        assertThat(datasetSnapshotOfFetch.getDatasetGid(), is(datasetSnapshot.getDatasetGid()));
        assertThat(datasetSnapshotOfFetch.getSchemaAt(), is(datasetSnapshot.getSchemaAt()));
        assertThat(datasetSnapshotOfFetch.getStatisticsAt(), is(datasetSnapshot.getStatisticsAt()));
        assertThat(JSONUtils.toJsonString(datasetSnapshotOfFetch.getSchemaSnapshot()), is(JSONUtils.toJsonString(datasetSnapshot.getSchemaSnapshot())));
        assertThat(JSONUtils.toJsonString(datasetSnapshotOfFetch.getStatisticsSnapshot()), is(JSONUtils.toJsonString(datasetSnapshot.getStatisticsSnapshot())));
    }

    @Test
    public void testFindByDatasetGid_withNotExistGid() {
        Long gid = -1L;
        List<DatasetSnapshot> datasetSnapshots = datasetSnapshotDao.findByDatasetGid(gid);

        assertTrue(datasetSnapshots.isEmpty());
    }

}
