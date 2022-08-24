package com.miotech.kun.dataquality.web.service;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.dataquality.core.expectation.Expectation;
import com.miotech.kun.dataquality.core.expectation.ValidationResult;
import com.miotech.kun.dataquality.web.common.dao.ExpectationRunDao;
import com.miotech.kun.dataquality.web.common.service.ExpectationService;
import com.miotech.kun.metadata.core.model.vo.DatasetDetail;
import com.miotech.kun.monitor.facade.alert.NotifyFacade;
import com.miotech.kun.monitor.facade.model.alert.AlertMessage;
import com.miotech.kun.workflow.core.event.TaskAttemptFinishedEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ExpectationFailureTaskAttemptFinishedEventHandler implements TaskAttemptFinishedEventHandler {

    @Value("${notify.urlLink.prefix}")
    private String prefix;

    @Autowired
    private ExpectationService expectationService;

    @Autowired
    private ExpectationRunDao expectationRunDao;

    @Autowired
    private MetadataClient metadataClient;

    @Autowired
    private NotifyFacade notifyFacade;

    @Override
    public void handle(TaskAttemptFinishedEvent event) {
        Long taskId = event.getTaskId();
        Expectation expectation = expectationService.fetchByTaskId(taskId);
        if (expectation == null) {
            return;
        }

        ValidationResult validationResult = expectationRunDao.fetchByExpectationId(expectation.getExpectationId());
        if (validationResult == null) {
            return;
        }

        if (!validationResult.isPassed()) {
            String notifiedUser = getNotifiedUser(expectation);
            if (notifiedUser == null) {
                log.warn("Dataset: {} does not set owner", expectation.getDataset().getGid());
                return;
            }

            String datasetName = getDatasetName(expectation.getDataset().getGid());
            long latestFailingCount = expectationRunDao.getLatestFailingCount(expectation.getExpectationId());
            log.info("expectation: {} failed, an alarm will be send to: {}", expectation.getExpectationId(), notifiedUser);
            String result = String.format("testcase '%s' failed", expectation.getName());
            String link = this.prefix + String.format("/data-discovery/dataset/%s?caseId=%s", expectation.getDataset().getGid(), expectation.getExpectationId());
            AlertMessage alertMessage = AlertMessage.newBuilder()
                    .withReason(AlertMessage.AlertReason.FAILURE)
                    .withDataset(datasetName)
                    .withResult(result)
                    .withOwner(notifiedUser)
                    .withNumberOfContinuousFailure(latestFailingCount)
                    .withLink(link)
                    .build();
            notifyFacade.notify(ImmutableList.of(notifiedUser), alertMessage.toMarkdown());
        }
    }

    private String getDatasetName(Long gid) {
        DatasetDetail datasetDetail = metadataClient.findById(gid);
        return datasetDetail.getDatabase() + "." + datasetDetail.getName();
    }

    private String getNotifiedUser(Expectation expectation) {
        // dataset owner
        DatasetDetail datasetDetail = metadataClient.findById(expectation.getDataset().getGid());
        List<String> owners = datasetDetail.getOwners();
        return CollectionUtils.isEmpty(owners) ? null : owners.get(0);
    }
}
