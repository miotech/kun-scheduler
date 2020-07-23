package com.miotech.kun.workflow.executor.local;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Injector;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.Executor;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.*;

@Singleton
public class LocalExecutor implements Executor {
    private static final Logger logger = LoggerFactory.getLogger(LocalExecutor.class);

    private static final int QUEUE_SIZE = 20000;

    @Inject
    private Injector injector;

    @Named("localExecutor.threadPool.coreSize")
    private Integer coreSize = 4;

    @Inject
    private TaskRunDao taskRunDao;

    @Inject
    private MiscService miscService;

    private final ExecutorService pool = new ThreadPoolExecutor(
            coreSize,
            coreSize,
            0,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(QUEUE_SIZE),
            new ThreadFactoryBuilder().setNameFormat("local-executor-worker-%d").build()
    );

    @Override
    public void submit(TaskAttempt taskAttempt) {
        logger.debug("Change TaskAttempt's status to QUEUED. taskAttempt={}", taskAttempt);
        miscService.changeTaskAttemptStatus(taskAttempt.getId(), TaskRunStatus.QUEUED);

        TaskInProgress tip = new TaskInProgress(taskAttempt);
        injector.injectMembers(tip);

        logger.debug("Submit TaskAttempt to pool. taskAttempt={}", taskAttempt);
        pool.submit(tip);
    }
}
