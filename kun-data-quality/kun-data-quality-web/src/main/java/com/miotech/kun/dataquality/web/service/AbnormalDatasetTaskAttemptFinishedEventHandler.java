package com.miotech.kun.dataquality.web.service;

import com.miotech.kun.dataplatform.facade.DeployedTaskFacade;
import com.miotech.kun.dataplatform.facade.model.deploy.DeployedTask;
import com.miotech.kun.dataquality.web.model.AbnormalDataset;
import com.miotech.kun.metadata.core.model.vo.DatasetDetail;
import com.miotech.kun.monitor.facade.alert.NotifyFacade;
import com.miotech.kun.monitor.facade.model.alert.AlertMessage;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.Task;
import com.miotech.kun.workflow.core.event.TaskAttemptFinishedEvent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AbnormalDatasetTaskAttemptFinishedEventHandler implements TaskAttemptFinishedEventHandler {

    @Value("${notify.urlLink.prefix}")
    private String prefix;

    @Autowired
    private AbnormalDatasetService abnormalDatasetService;

    @Autowired
    private MetadataClient metadataClient;

    @Autowired
    private WorkflowClient workflowClient;

    @Autowired
    private DeployedTaskFacade deployedTaskFacade;

    @Autowired
    private NotifyFacade notifyFacade;

    @Override
    public void handle(TaskAttemptFinishedEvent event) {
        AbnormalDataset abnormalDataset = abnormalDatasetService.findByTaskRunId(event.getTaskRunId());
        if (abnormalDataset == null) {
            return;
        }

        abnormalDatasetService.updateStatusByTaskRunId(event.getTaskRunId(), event.getFinalStatus().isSuccess() ? "SUCCESS" : "FAILED");
        boolean isAlert = !event.getFinalStatus().isSuccess();
        Task task = workflowClient.getTask(event.getTaskId());
        Optional<DeployedTask> deployedTaskOpt = deployedTaskFacade.findByWorkflowTaskId(event.getTaskId());
        DatasetDetail datasetDetail = metadataClient.findById(abnormalDataset.getDatasetGid());
        if (isAlert) {
            String result = "data update failed";
            String link = this.prefix + String.format("/data-development/task-definition/%s", deployedTaskOpt.get().getDefinitionId());
            AlertMessage alertMessage = AlertMessage.newBuilder()
                    .withReason(AlertMessage.AlertReason.FAILURE)
                    .withDataset(datasetDetail.getDatabase() + "." + datasetDetail.getName())
                    .withResult(result)
                    .withOwner(StringUtils.join(datasetDetail.getOwners(), ","))
                    .withUpstreamTask(task.getName())
                    .withLink(link)
                    .build();
            notifyFacade.notify(datasetDetail.getOwners(), alertMessage.toMarkdown());
        }
    }

}
