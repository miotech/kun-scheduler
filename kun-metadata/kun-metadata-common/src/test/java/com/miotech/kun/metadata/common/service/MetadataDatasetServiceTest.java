package com.miotech.kun.metadata.common.service;

import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.metadata.common.dao.MetadataDatasetDao;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;

public class MetadataDatasetServiceTest extends DatabaseTestBase {
    private MetadataDatasetDao metadataDatasetDao = Mockito.mock(MetadataDatasetDao.class);

    private MetadataDatasetService metadataDatasetService = new MetadataDatasetService(metadataDatasetDao);

    @Test
    public void fetchDatasetByGid_shouldInvokeMetadataDatasetDao() {
        // Prepare
        AtomicLong passedArgument = new AtomicLong();

        Mockito.doAnswer((invocation) -> {
            Long gidArgument = invocation.getArgument(0);
            passedArgument.set(gidArgument);
            return Optional.ofNullable(null);
        }).when(metadataDatasetDao).fetchDatasetByGid(anyLong());

        // Process
        Long queryGid = IdGenerator.getInstance().nextId();
        metadataDatasetService.fetchDatasetByGid(queryGid);

        // Validate
        assertThat(passedArgument.get(), is(queryGid.longValue()));
    }
}
