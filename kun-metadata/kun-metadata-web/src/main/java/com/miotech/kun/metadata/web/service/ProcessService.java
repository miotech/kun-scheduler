package com.miotech.kun.metadata.web.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.common.dao.PullProcessDao;
import com.miotech.kun.metadata.core.model.process.PullDataSourceProcess;
import com.miotech.kun.metadata.core.model.process.PullDatasetProcess;
import com.miotech.kun.metadata.databuilder.constant.DataBuilderDeployMode;
import com.miotech.kun.metadata.web.constant.PropKey;
import com.miotech.kun.metadata.web.constant.TaskParam;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Singleton
public class ProcessService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessService.class);

    private static final ConcurrentMap<Long, Lock> pullLocks = new ConcurrentHashMap<>();

    @Inject
    private PullProcessDao pullProcessDao;

    @Inject
    private WorkflowClient workflowClient;

    @Inject
    private Props props;

    public TaskRun submit(Long id, DataBuilderDeployMode deployMode) {
        // For each data entity, we only allow atomic operations on creating new pull task run
        Lock pullLock = pullLocks.putIfAbsent(id, new ReentrantLock());
        Preconditions.checkState(pullLock != null, "Unexpected state: pull lock for specific id: {} is null", id);
        // Lock that id to prevent multiple MCE task runs created for the same data entity at same time
        pullLock.lock();
        try {
            switch (deployMode) {
                case DATASOURCE:
                    return submitPullDatasourceTaskIfLastOneFinished(id, deployMode);
                case DATASET:
                    return submitPullDatasetTaskIfLastOneFinished(id, deployMode);
                default:
                    return createPullTaskRunInstanceOnWorkflow(id, deployMode);
            }
        } catch (Exception e) {
            logger.error("Failed to submit task to workflow for datasource with id: {}.", id);
            throw e;
        } finally {
            // always release the lock
            pullLock.unlock();
        }
    }

    private TaskRun submitPullDatasourceTaskIfLastOneFinished(Long datasourceId, DataBuilderDeployMode deployMode) {
        Optional<PullDataSourceProcess> latestProcess =
                pullProcessDao.findLatestPullDataSourceProcessByDataSourceId(datasourceId.toString());
        // 1. If last pulling process has not finished yet, do not submit a new MCE task run.
        if (latestProcess.isPresent() && (latestProcess.get().getMceTaskRunId() != null)) {
            TaskRun latestMCETaskRun = workflowClient.getTaskRun(latestProcess.get().getMceTaskRunId());
            if (!latestMCETaskRun.getStatus().isFinished()) {
                // Instead we return the existing running task run instance.
                return latestMCETaskRun;
            }
        }
        // 2. If not, create a new MCE task
        TaskRun mceTaskRun = createPullTaskRunInstanceOnWorkflow(datasourceId, deployMode);
        // ...and record that MCE task run id into a new process record
        pullProcessDao.create(PullDataSourceProcess.newBuilder()
                .withDataSourceId(datasourceId.toString())
                .withCreatedAt(DateTimeUtils.now())
                .withMceTaskRunId(mceTaskRun.getId())
                .build()
        );
        return mceTaskRun;
    }

    private TaskRun submitPullDatasetTaskIfLastOneFinished(Long datasetId, DataBuilderDeployMode deployMode) {
        Optional<PullDatasetProcess> latestProcess =
                pullProcessDao.findLatestPullDatasetProcessByDataSetId(datasetId.toString());
        // 1. If last pulling process has not finished yet, do not submit a new MCE task run.
        if (latestProcess.isPresent() && (latestProcess.get().getMceTaskRunId() != null)) {
            TaskRun latestMCETaskRun = workflowClient.getTaskRun(latestProcess.get().getMceTaskRunId());
            if (!latestMCETaskRun.getStatus().isFinished()) {
                // Instead we return the existing running task run instance.
                return latestMCETaskRun;
            }
        }
        // 2. If not, create a new MCE task
        TaskRun mceTaskRun = createPullTaskRunInstanceOnWorkflow(datasetId, deployMode);
        // ...and record that MCE task run id into a new process record
        pullProcessDao.create(PullDatasetProcess.newBuilder()
                .withDatasetId(datasetId.toString())
                .withCreatedAt(DateTimeUtils.now())
                .withMceTaskRunId(mceTaskRun.getId())
                // TODO: figure out how to fetch MSE task run id which created asynchronously.
                .withMseTaskRunId(null)
                .build()
        );
        return mceTaskRun;
    }

    /**
     * Create a pull task run instance (usually a MCE task run) on workflow
     * @param id id of target data entity (datasource / dataset)
     * @param deployMode deploy mode
     * @return created task run instance on workflow
     */
    private TaskRun createPullTaskRunInstanceOnWorkflow(Long id, DataBuilderDeployMode deployMode) {
        TaskRun taskRun = workflowClient.executeTask(props.getLong(TaskParam.MCE_TASK.getName()),
                buildVariablesForTaskRun(deployMode, id.toString()));
        if (taskRun == null) {
            throw new IllegalStateException("Workflow returns a null task run instance.");
        }
        return taskRun;
    }

    public TaskRun fetchStatus(String id) {
        Preconditions.checkNotNull(id, "Invalid id: null");
        return workflowClient.getTaskRun(Long.parseLong(id));
    }

    private Map<String, Object> buildVariablesForTaskRun(DataBuilderDeployMode deployMode, String id) {
        Map<String, Object> conf = Maps.newHashMap();
        conf.put(PropKey.JDBC_URL, props.get(PropKey.JDBC_URL));
        conf.put(PropKey.USERNAME, props.get(PropKey.USERNAME));
        conf.put(PropKey.PASSWORD, props.get(PropKey.PASSWORD));
        conf.put(PropKey.DRIVER_CLASS_NAME, props.get(PropKey.DRIVER_CLASS_NAME));
        conf.put(PropKey.DEPLOY_MODE, deployMode.name());
        conf.put(PropKey.BROKERS, props.get("kafka.bootstrapServers"));
        conf.put(PropKey.MSE_TOPIC, props.get("kafka.mseTopicName"));

        switch (deployMode) {
            case DATASOURCE:
                conf.put(PropKey.DATASOURCE_ID, id);
                break;
            case DATASET:
                conf.put(PropKey.GID, id);
                break;
            default:
                throw new UnsupportedOperationException("Invalid deployMode:" + deployMode);
        }

        return conf;
    }
}
