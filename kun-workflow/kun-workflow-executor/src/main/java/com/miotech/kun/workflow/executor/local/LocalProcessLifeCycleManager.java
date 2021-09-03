package com.miotech.kun.workflow.executor.local;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.common.exception.EntityNotFoundException;
import com.miotech.kun.workflow.common.operator.dao.OperatorDao;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.execution.ExecCommand;
import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.worker.WorkerInstance;
import com.miotech.kun.workflow.core.model.worker.WorkerSnapshot;
import com.miotech.kun.workflow.executor.AbstractQueueManager;
import com.miotech.kun.workflow.executor.WorkerLifeCycleManager;
import com.miotech.kun.workflow.executor.WorkerMonitor;
import com.miotech.kun.workflow.core.model.worker.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class LocalProcessLifeCycleManager extends WorkerLifeCycleManager {

    private static final Logger logger = LoggerFactory.getLogger(LocalProcessLifeCycleManager.class);

    private final OperatorDao operatorDao;

    private final LocalProcessBackend localProcessBackend;

    private static final Integer DB_MAX_POOL = 1;
    private static final Integer MINI_MUM_IDLE = 0;

    @Inject
    public LocalProcessLifeCycleManager(TaskRunDao taskRunDao, WorkerMonitor workerMonitor, Props props,
                                        MiscService miscService, AbstractQueueManager queueManager,
                                        OperatorDao operatorDao, LocalProcessBackend localProcessBackend) {
        super(taskRunDao, workerMonitor, props, miscService, queueManager);
        this.operatorDao = operatorDao;
        this.localProcessBackend = localProcessBackend;
    }

    @Override
    public void startWorker(TaskAttempt taskAttempt) {
        ExecCommand command = buildExecCommand(taskAttempt);
        localProcessBackend.startProcess(command, buildDatabaseConfig());
    }

    @Override
    public Boolean stopWorker(Long taskAttemptId) {
        ProcessSnapShot processSnapShot = localProcessBackend.fetchProcessByTaskAttemptId(taskAttemptId);
        if (processSnapShot == null) {
            return true;
        }
        String processId = processSnapShot.getIns().getWorkerId();

        logger.info("worker going to shutdown, taskAttemptId = {}", taskAttemptId);
        Runtime rt = Runtime.getRuntime();
        try {
            Process process;
            if (System.getProperty("os.name").toLowerCase().indexOf("windows") > -1)
                process = rt.exec("taskkill " + processId);
            else {
                process = rt.exec("kill " + processId);
            }
            int exitCode = process.waitFor();
            return exitCode == 0 ? true : false;

        } catch (IOException | InterruptedException e) {
            logger.error("force kill worker failed processId = {} , taskAttemptId = {}", processId, taskAttemptId);
        }
        return false;
    }

    @Override
    public WorkerSnapshot getWorker(Long taskAttemptId) {
        return localProcessBackend.fetchProcessByTaskAttemptId(taskAttemptId);
    }

    @Override
    public String getWorkerLog(Long taskAttemptId, Integer tailLines) {
        throw new UnsupportedOperationException(" not support get log from local process yet");
    }

    @Override
    public List<WorkerInstance> getRunningWorker() {
        List<ProcessSnapShot> processList = localProcessBackend.fetchRunningProcess();
        return processList.stream().map(WorkerSnapshot::getIns).collect(Collectors.toList());
    }

    private ExecCommand buildExecCommand(TaskAttempt attempt) {
        Long attemptId = attempt.getId();
        // Operator信息
        Long operatorId = attempt.getTaskRun().getTask().getOperatorId();
        Operator operatorDetail = operatorDao.fetchById(operatorId)
                .orElseThrow(EntityNotFoundException::new);
        logger.debug("Fetched operator's details. operatorId={}, details={}", operatorId, operatorDetail);

        ExecCommand command = new ExecCommand();
        command.setRegisterUrl(props.getString("rpc.registry"));
        command.setTaskAttemptId(attemptId);
        command.setTaskRunId(attempt.getTaskRun().getId());
        command.setKeepAlive(false);
        command.setConfig(attempt.getTaskRun().getConfig());
        command.setLogPath(attempt.getLogPath());
        command.setJarPath(operatorDetail.getPackagePath());
        command.setClassName(operatorDetail.getClassName());
        command.setQueueName(attempt.getQueueName());
        logger.debug("Execute task. attemptId={}, command={}", attemptId, command);
        return command;
    }

    private DatabaseConfig buildDatabaseConfig() {
        DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.setDatasourceUrl(props.get("datasource.jdbcUrl"));
        databaseConfig.setDatasourceUser(props.get("datasource.username"));
        databaseConfig.setDatasourcePassword(props.get("datasource.password"));
        databaseConfig.setDatasourceDriver(props.get("datasource.driverClassName"));
        databaseConfig.setDatasourceMaxPoolSize(DB_MAX_POOL);
        databaseConfig.setDatasourceMinIdle(MINI_MUM_IDLE);
        databaseConfig.setNeo4juri(props.get("neo4j.uri"));
        databaseConfig.setNeo4jUser(props.get("neo4j.username"));
        databaseConfig.setNeo4jPassword(props.get("neo4j.password"));
        return databaseConfig;
    }

}
