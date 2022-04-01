package com.miotech.kun.dataquality.web.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.miotech.kun.dataquality.core.expectation.Expectation;
import com.miotech.kun.dataquality.core.expectation.ValidationResult;
import com.miotech.kun.dataquality.web.common.dao.ExpectationRunDao;
import com.miotech.kun.dataquality.web.common.service.ExpectationService;
import com.miotech.kun.metadata.core.model.vo.DatasetDetail;
import com.miotech.kun.monitor.facade.alert.NotifyFacade;
import com.miotech.kun.workflow.core.event.TaskAttemptFinishedEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class ExpectationFailureTaskAttemptFinishedEventHandler implements TaskAttemptFinishedEventHandler {

    private static final String MSG_TEMPLATE = "Test case: '%s' failed.%n%nSee link: %s";

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
            List<String> notifiedUsers = getNotifiedUsers(expectation);
            log.info("expectation: {} failed, an alarm will be send to: {}", expectation.getExpectationId(), StringUtils.join(notifiedUsers, ","));
            notifyFacade.notify(notifiedUsers, String.format(MSG_TEMPLATE, expectation.getName(),
                    this.prefix + String.format("/data-discovery/dataset/%s?caseId=%s", expectation.getDataset().getGid(), expectation.getExpectationId())));
        }
    }

    private List<String> getNotifiedUsers(Expectation expectation) {
        Set<String> notifiedUsers = Sets.newHashSet();

        // add dataset owner
        DatasetDetail datasetDetail = metadataClient.findById(expectation.getDataset().getGid());
        List<String> owners = datasetDetail.getOwners();
        notifiedUsers.addAll(owners);

        // add expectation owner
        notifiedUsers.add(expectation.getUpdateUser());

        List<String> result = Lists.newArrayList();
        result.addAll(notifiedUsers);
        return result;
    }
}
