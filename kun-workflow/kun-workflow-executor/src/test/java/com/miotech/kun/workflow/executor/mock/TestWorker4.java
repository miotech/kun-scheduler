package com.miotech.kun.workflow.executor.mock;

import com.google.common.util.concurrent.Uninterruptibles;
import com.miotech.kun.workflow.core.execution.ExecCommand;
import com.miotech.kun.workflow.core.execution.HeartBeatMessage;
import com.miotech.kun.workflow.core.execution.OperatorReport;
import com.miotech.kun.workflow.core.execution.TaskAttemptMsg;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.facade.WorkflowExecutorFacade;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import com.miotech.kun.workflow.worker.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class TestWorker4 implements Worker {
    private WorkflowExecutorFacade workflowExecutorFacade;

    private TaskAttemptMsg taskAttemptMsg;

    private static volatile boolean exit = false;

    private final static Logger logger = LoggerFactory.getLogger(TestWorker4.class);


    public TestWorker4(WorkflowExecutorFacade workflowExecutorFacade) {
        this.workflowExecutorFacade = workflowExecutorFacade;
    }

    @Override
    public void killTask(Boolean abort) {
        logger.info("worker going to shutdown");
        exit = true;
    }

    @Override
    public void start(ExecCommand command) {
        new Thread(new TestWorker4.TestOperatorLaunch(command)).start();
    }

    @Override
    public boolean shutdown() {
        return false;
    }

    class TestOperatorLaunch implements Runnable {

        private ExecCommand command;

        public TestOperatorLaunch(ExecCommand command) {
            this.command = command;
        }

        @Override
        public void run() {
            logger.info("init worker ....");
            HeartBeatMessage message = new HeartBeatMessage();
            message.setWorkerId(1l);
            message.setTaskAttemptId(command.getTaskAttemptId());
            message.setTaskRunId(command.getTaskRunId());
            message.setTaskRunStatus(TaskRunStatus.RUNNING);
            message.setPort(1);
            message.setTimeoutTimes(0);
            workflowExecutorFacade.heartBeat(message);
            taskAttemptMsg = new TaskAttemptMsg();
            taskAttemptMsg.setTaskAttemptId(command.getTaskAttemptId());
            taskAttemptMsg.setTaskRunStatus(TaskRunStatus.RUNNING);
            taskAttemptMsg.setTaskRunId(command.getTaskRunId());
            taskAttemptMsg.setStartAt(DateTimeUtils.now());
            taskAttemptMsg.setWorkerId(4l);
            taskAttemptMsg.setQueueName("default");
            logger.info("START RUNNING");
            taskAttemptMsg.setWorkerId(10004l);
            Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
            workflowExecutorFacade.statusUpdate(taskAttemptMsg);
            TaskAttemptMsg finish = taskAttemptMsg.copy();
            finish.setTaskRunStatus(TaskRunStatus.SUCCESS);
            finish.setOperatorReport(new OperatorReport());
            finish.setEndAt(DateTimeUtils.now());
            for (int i = 0; i < 3; i++) {
                taskAttemptMsg.setTaskRunStatus(TaskRunStatus.SUCCESS);
                try {
                    workflowExecutorFacade.statusUpdate(finish);
                }catch (IllegalStateException e){
                    logger.error("status update failed",e);
                }
            }

        }
    }
}
