package com.miotech.kun.dataquality;

import com.miotech.kun.dataquality.mock.MockDataQualityFactory;
import com.miotech.kun.dataquality.model.bo.DataQualityRequest;
import com.miotech.kun.dataquality.model.entity.DataQualityCaseBasic;
import com.miotech.kun.dataquality.persistence.DataQualityRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DataQualityRepositoryTest extends DataQualityTestBase {

    @Autowired
    private DataQualityRepository dataQualityRepository;

    @Test
    public void fetchCaseBasicByTaskId(){
        DataQualityRequest dataQualityRequest = MockDataQualityFactory.createRequest();
        dataQualityRepository.addCase(dataQualityRequest);
        DataQualityCaseBasic savedBasic = dataQualityRepository.fetchCaseBasicByTaskId(dataQualityRequest.getTaskId());
        assertThat(savedBasic.getTaskId(),is(dataQualityRequest.getTaskId()));
        assertThat(savedBasic.getName(),is(dataQualityRequest.getName()));
        assertThat(savedBasic.getIsBlock(),is(dataQualityRequest.getIsBlock()));

    }
}
