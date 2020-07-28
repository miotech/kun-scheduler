package com.miotech.kun.workflow.executor.local;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Injector;
import com.miotech.kun.workflow.common.resource.ResourceLoader;
import com.miotech.kun.workflow.common.taskrun.service.TaskRunService;
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

    // Max timeout: 10s
    private static final int MAX_TIMEOUT_MILLISECONDS = 10000;

    private final Injector injector;

    @Named("localExecutor.threadPool.coreSize")
    private final Integer coreSize = Runtime.getRuntime().availableProcessors() * 4;

    private final TaskRunService taskRunService;

    private final ResourceLoader resourceLoader;

    @Inject
    public LocalExecutor(Injector injector, TaskRunService taskRunService, ResourceLoader resourceLoader) {
        this.injector = injector;
        this.taskRunService = taskRunService;
        this.resourceLoader = resourceLoader;
        init();
    }

    private void init() {
        attachSiftingAppender();
    }

    private final ConcurrentMap<Long, TaskInProgress> lookupMap = new ConcurrentHashMap<>();

    private final ListeningExecutorService pool = MoreExecutors.listeningDecorator(new ThreadPoolExecutor(
            coreSize,
            coreSize,
            0,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(QUEUE_SIZE),
            new ThreadFactoryBuilder().setNameFormat("local-executor-worker-%d").build()
    ));

    @Override
    public void submit(TaskAttempt taskAttempt) {
        TaskInProgress tip = new TaskInProgress(taskAttempt);
        injector.injectMembers(tip);
        lookupMap.put(taskAttempt.getId(), tip);

        logger.debug("Change TaskAttempt's status to QUEUED. taskAttempt={}", taskAttempt);
        tip.changeStatus(TaskRunStatus.QUEUED, null, null);

        logger.debug("Submit TaskAttempt to pool. taskAttempt={}", taskAttempt);
        ListenableFuture<Void> f = pool.submit(tip, null);
        f.addListener(() -> {
            lookupMap.remove(taskAttempt.getId());
        }, MoreExecutors.directExecutor());
        tip.setRunFuture(f);
    }

    @Override
    public boolean cancel(Long taskAttemptId) {
        TaskInProgress tip = lookupMap.get(taskAttemptId);
        if (tip.isCompleted()) {
            return false;
        }
        return tryAbortTask(tip);
    }

    private boolean tryAbortTask(TaskInProgress tip) {
        Future<Void> runFuture = tip.abort();
        try {
            runFuture.get(MAX_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);
            return true;
        } catch (TimeoutException e) {
            logger.debug("Self-aborting timeout. Enforcing abort for task in progress {}...", tip);
            tip.forceAbort();
            return true;
        } catch (InterruptedException e) {
            logger.error("InterruptedException occurs when try abort task: ", e);
            Thread.currentThread().interrupt();
            return false;
        } catch (ExecutionException e) {
            logger.error("ExecutionException occurs when try abort task:", e);
            return false;
        }
    }

    private void attachSiftingAppender() {
        ch.qos.logback.classic.Logger rootLogger
                = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        TaskAttemptSiftingAppender a = new TaskAttemptSiftingAppender(resourceLoader, taskRunService);
        a.setContext(rootLogger.getLoggerContext());
        a.start();
        rootLogger.addAppender(a);
    }
}
