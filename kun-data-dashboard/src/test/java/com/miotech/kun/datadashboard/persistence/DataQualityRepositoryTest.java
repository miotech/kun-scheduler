package com.miotech.kun.datadashboard.persistence;

import com.miotech.kun.datadashboard.DataDashboardTestBase;
import com.miotech.kun.datadashboard.model.bo.TestCasesRequest;
import com.miotech.kun.datadashboard.model.entity.AbnormalDatasets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

public class DataQualityRepositoryTest extends DataDashboardTestBase {

    @Autowired
    private DataQualityRepository dataQualityRepository;

    @Test
    public void testGetAbnormalDatasets_empty() {
        TestCasesRequest request = new TestCasesRequest();
        AbnormalDatasets abnormalDatasets = dataQualityRepository.getAbnormalDatasets(request);
        assertThat(abnormalDatasets.getAbnormalDatasets(), empty());
    }
}
