package com.miotech.kun.metadata.web.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.common.dao.PullProcessDao;
import com.miotech.kun.metadata.core.model.process.PullDataSourceProcess;
import com.miotech.kun.metadata.core.model.process.PullDatasetProcess;
import com.miotech.kun.metadata.core.model.process.PullProcess;
import com.miotech.kun.metadata.databuilder.constant.DataBuilderDeployMode;
import com.miotech.kun.metadata.web.constant.PropKey;
import com.miotech.kun.metadata.web.constant.TaskParam;
import com.miotech.kun.metadata.web.model.vo.PullProcessVO;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.facade.WorkflowServiceFacade;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.miotech.kun.metadata.web.constant.PropKey.INFRA_URL;

@Singleton
public class ProcessService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessService.class);

    private static final ConcurrentMap<Long, Lock> pullLocks = new ConcurrentHashMap<>();

    @Inject
    private PullProcessDao pullProcessDao;

    @Inject
    private WorkflowServiceFacade workflowServiceFacade;

    @Inject
    private Props props;

    public PullProcessVO submitPull(Long id, DataBuilderDeployMode deployMode) {
        // For each data entity, we only allow atomic operations on creating new pull task run
        if (!pullLocks.containsKey(id)) {
            pullLocks.put(id, new ReentrantLock());
        }
        Lock pullLock = pullLocks.get(id);
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
                    // Currently, this API only support pulling operation on data source and dataset.
                    // "PULL" and "ALL" mode should be invoked with transferring messages by Kafka
                    throw new UnsupportedOperationException(String.format("Deploy mode \"%s\" not supported yet.", deployMode.name()));
            }
        } catch (Exception e) {
            logger.error("Failed to submit task to workflow for datasource with id: {}.", id);
            throw e;
        } finally {
            // always release the lock
            pullLock.unlock();
        }
    }

    public Map<Long, PullProcessVO> fetchLatestProcessByDataSourceIds(Collection<Long> dataSourceIds) {
        Map<Long, PullDataSourceProcess> latestProcessesMap = pullProcessDao.findLatestPullDataSourceProcessesByDataSourceIds(dataSourceIds);
        Map<Long, PullProcessVO> result = new HashMap<>();
        for (Map.Entry<Long, PullDataSourceProcess> entry : latestProcessesMap.entrySet()) {
            PullDataSourceProcess process = entry.getValue();
            TaskRun latestTaskRun = workflowServiceFacade.getTaskRun(process.getMceTaskRunId());
            PullProcessVO processVO = new PullProcessVO(
                    process.getProcessId(),
                    process.getProcessType(),
                    process.getCreatedAt(),
                    latestTaskRun,
                    null
            );
            result.put(entry.getKey(), processVO);
        }
        return result;
    }

    public Optional<PullProcessVO> fetchLatestProcessByDatasetId(Long datasetId) {
        Optional<PullDatasetProcess> pullDatasetProcessOptional = pullProcessDao.findLatestPullDatasetProcessByDataSetId(datasetId);
        if (!pullDatasetProcessOptional.isPresent()) {
            return Optional.empty();
        }
        // else
        PullDatasetProcess process = pullDatasetProcessOptional.get();
        TaskRun latestTaskRun = workflowServiceFacade.getTaskRun(process.getMceTaskRunId());
        PullProcessVO vo = new PullProcessVO(
                process.getProcessId(),
                process.getProcessType(),
                process.getCreatedAt(),
                latestTaskRun,
                // TODO: figure out how to obtain MSE task runs
                null
        );
        return Optional.of(vo);
    }

    private PullProcessVO submitPullDatasourceTaskIfLastOneFinished(Long datasourceId, DataBuilderDeployMode deployMode) {
        Optional<PullDataSourceProcess> latestProcess =
                pullProcessDao.findLatestPullDataSourceProcessByDataSourceId(datasourceId);
        // 1. If last pulling process has not finished yet, do not submit a new MCE task run.
        if (latestProcess.isPresent() && (latestProcess.get().getMceTaskRunId() != null)) {
            TaskRun latestMCETaskRun = workflowServiceFacade.getTaskRun(latestProcess.get().getMceTaskRunId());
            if (!latestMCETaskRun.getStatus().isFinished()) {
                // Instead we return the existing running task run instance.
                return new PullProcessVO(
                        latestProcess.get().getProcessId(),
                        latestProcess.get().getProcessType(),
                        latestProcess.get().getCreatedAt(),
                        latestMCETaskRun,
                        null
                );
            }
        }
        // 2. If not, create a new MCE task
        TaskRun mceTaskRun = createPullTaskRunInstanceOnWorkflow(datasourceId, deployMode);
        // ...and record that MCE task run id into a new process record
        PullProcess createdProcess = pullProcessDao.create(PullDataSourceProcess.newBuilder()
                .withDataSourceId(datasourceId)
                .withCreatedAt(DateTimeUtils.now())
                .withMceTaskRunId(mceTaskRun.getId())
                .build()
        );
        return new PullProcessVO(
                createdProcess.getProcessId(),
                createdProcess.getProcessType(),
                createdProcess.getCreatedAt(),
                mceTaskRun,
                null
        );
    }

    private PullProcessVO submitPullDatasetTaskIfLastOneFinished(Long datasetId, DataBuilderDeployMode deployMode) {
        Optional<PullDatasetProcess> latestProcess =
                pullProcessDao.findLatestPullDatasetProcessByDataSetId(datasetId);
        // 1. If last pulling process has not finished yet, do not submit a new MCE task run.
        if (latestProcess.isPresent() && (latestProcess.get().getMceTaskRunId() != null)) {
            TaskRun latestMCETaskRun = workflowServiceFacade.getTaskRun(latestProcess.get().getMceTaskRunId());
            if (!latestMCETaskRun.getStatus().isFinished()) {
                // Instead we return the existing running task run instance.
                return new PullProcessVO(
                        latestProcess.get().getProcessId(),
                        latestProcess.get().getProcessType(),
                        latestProcess.get().getCreatedAt(),
                        latestMCETaskRun,
                        null
                );
            }
        }
        // 2. If not, create a new MCE task
        TaskRun mceTaskRun = createPullTaskRunInstanceOnWorkflow(datasetId, deployMode);
        // ...and record that MCE task run id into a new process record
        PullProcess createdProcess = pullProcessDao.create(PullDatasetProcess.newBuilder()
                .withDatasetId(datasetId)
                .withCreatedAt(DateTimeUtils.now())
                .withMceTaskRunId(mceTaskRun.getId())
                // TODO: figure out how to fetch MSE task run id which created asynchronously.
                .withMseTaskRunId(null)
                .build()
        );
        return new PullProcessVO(
                createdProcess.getProcessId(),
                createdProcess.getProcessType(),
                createdProcess.getCreatedAt(),
                mceTaskRun,
                null
        );
    }

    /**
     * Create a pull task run instance (usually a MCE task run) on workflow
     * @param id id of target data entity (datasource / dataset)
     * @param deployMode deploy mode
     * @return created task run instance on workflow
     */
    private TaskRun createPullTaskRunInstanceOnWorkflow(Long id, DataBuilderDeployMode deployMode) {
        TaskRun taskRun = workflowServiceFacade.executeTask(props.getLong(TaskParam.MCE_TASK.getName()),
                buildVariablesForTaskRun(deployMode, id.toString()));
        if (taskRun == null) {
            throw new IllegalStateException("Workflow returns a null task run instance.");
        }
        return taskRun;
    }

    public TaskRun fetchStatus(String id) {
        Preconditions.checkNotNull(id, "Invalid id: null");
        return workflowServiceFacade.getTaskRun(Long.parseLong(id));
    }

    private Map<String, Object> buildVariablesForTaskRun(DataBuilderDeployMode deployMode, String id) {
        Map<String, Object> conf = Maps.newHashMap();
        conf.put(PropKey.JDBC_URL, props.get(PropKey.JDBC_URL));
        conf.put(PropKey.USERNAME, props.get(PropKey.USERNAME));
        conf.put(PropKey.PASSWORD, props.get(PropKey.PASSWORD));
        conf.put(PropKey.DRIVER_CLASS_NAME, props.get(PropKey.DRIVER_CLASS_NAME));
        conf.put(PropKey.DEPLOY_MODE, deployMode.name());
        conf.put(PropKey.MSE_URL, props.getString(INFRA_URL) + "/mse/_execute");

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
