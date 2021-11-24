package com.miotech.kun.dataquality.web.service;

import com.cronutils.model.Cron;
import com.google.common.collect.ImmutableList;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.dataquality.web.model.AbnormalDataset;
import com.miotech.kun.dataquality.web.persistence.AbnormalDatasetRepository;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.core.event.TaskRunCreatedEvent;
import com.miotech.kun.workflow.core.model.lineage.node.DatasetInfo;
import com.miotech.kun.workflow.utils.CronUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class AbnormalDatasetService {

    private static final List<String> IGNORED_QUEUE_NAMES = ImmutableList.of("pdf", "metadata", "pdf-dev");

    @Value("${data-quality.daily.start:0 0 0 * * ?}")
    private String scheduleAt;

    @Autowired
    private WorkflowClient workflowClient;

    @Autowired
    private AbnormalDatasetRepository abnormalDatasetRepository;

    public AbnormalDataset create(AbnormalDataset abnormalDataset, Long datasetGid) {
        abnormalDataset = abnormalDataset.cloneBuilder()
                .withDatasetGid(datasetGid)
                .withCreateTime(DateTimeUtils.now())
                .withUpdateTime(DateTimeUtils.now())
                .withScheduleAt(generateScheduleAt())
                .build();
        abnormalDatasetRepository.create(abnormalDataset);
        return abnormalDataset;
    }

    public List<AbnormalDataset> fetchByScheduleAtAndStatusIsNull(String scheduleAt) {
        return abnormalDatasetRepository.fetchByScheduleAtAndStatusIsNull(scheduleAt);
    }

    public boolean updateStatus(Long id, String status) {
        return abnormalDatasetRepository.updateStatus(id, status);
    }

    private String generateScheduleAt() {
        Cron cron = CronUtils.convertStringToCron(scheduleAt);
        Optional<OffsetDateTime> nextExecutionTimeOpt = CronUtils.getNextExecutionTimeFromNow(cron);
        if (!nextExecutionTimeOpt.isPresent()) {
            return null;
        }

        OffsetDateTime nextExecutionTime = nextExecutionTimeOpt.get();
        return nextExecutionTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    public void handleTaskRunCreatedEvent(TaskRunCreatedEvent event) {
        TaskRun taskRun = workflowClient.getTaskRun(event.getTaskRunId());
        if (filter(taskRun)) {
            Set<DatasetInfo> datasetNodes = workflowClient.fetchOutletNodes(taskRun.getTask().getId());
            datasetNodes.stream().forEach(dn -> create(AbnormalDataset.from(taskRun), dn.getGid()));
        }
    }

    private boolean filter(TaskRun taskRun) {
        return !IGNORED_QUEUE_NAMES.contains(taskRun.getQueueName());
    }
}
