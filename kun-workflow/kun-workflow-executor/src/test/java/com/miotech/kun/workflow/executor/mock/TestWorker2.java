package com.miotech.kun.workflow.executor.mock;

import com.miotech.kun.workflow.core.execution.ExecCommand;
import com.miotech.kun.workflow.core.execution.HeartBeatMessage;
import com.miotech.kun.workflow.core.execution.TaskAttemptMsg;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.facade.WorkflowExecutorFacade;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import com.miotech.kun.workflow.worker.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestWorker2 implements Worker {
    private WorkflowExecutorFacade workflowExecutorFacade;

    private TaskAttemptMsg taskAttemptMsg;

    private static volatile boolean exit = false;

    private final static Logger logger = LoggerFactory.getLogger(TestWorker2.class);


    public TestWorker2(WorkflowExecutorFacade workflowExecutorFacade) {
        this.workflowExecutorFacade = workflowExecutorFacade;
    }

    @Override
    public void killTask(Boolean abort) {
        logger.info("worker going to shutdown");
        exit = true;
    }

    @Override
    public void start(ExecCommand command) {
        new Thread(new TestWorker2.TestOperatorLaunch(command)).start();
    }

    class TestOperatorLaunch implements Runnable {

        private ExecCommand command;

        public TestOperatorLaunch(ExecCommand command) {
            this.command = command;
        }

        @Override
        public void run() {
            try {
                logger.info("init worker ....");
                Thread.sleep(1000);
            } catch (Exception e) {

            }
            logger.info("START RUNNING");
            taskAttemptMsg = new TaskAttemptMsg();
            taskAttemptMsg.setTaskAttemptId(command.getTaskAttemptId());
            taskAttemptMsg.setTaskRunStatus(TaskRunStatus.RUNNING);
            taskAttemptMsg.setTaskRunId(command.getTaskRunId());
            taskAttemptMsg.setStartAt(DateTimeUtils.now());
            taskAttemptMsg.setWorkerId(1l);
            workflowExecutorFacade.statusUpdate(taskAttemptMsg);
            HeartBeatMessage message = new HeartBeatMessage();
            message.setWorkerId(1l);
            message.setTaskAttemptId(command.getTaskAttemptId());
            message.setTaskRunId(command.getTaskRunId());
            message.setTaskRunStatus(TaskRunStatus.RUNNING);
            message.setPort(11111);
            for (int i = 0; i < 3; i++) {
                if(exit){
                    logger.info("shutdown worker");
                    return;
                }
                try {
                    logger.info("running worker send heartbeat");
                    message.setTimeoutTimes(0);
                    workflowExecutorFacade.heartBeat(message);
                    Thread.currentThread().sleep(5000);
                } catch (Exception e) {
                    logger.error("send heartbeat failed",e);
                }
            }
            taskAttemptMsg.setTaskRunStatus(TaskRunStatus.SUCCESS);
            workflowExecutorFacade.statusUpdate(taskAttemptMsg);
        }
    }

}
