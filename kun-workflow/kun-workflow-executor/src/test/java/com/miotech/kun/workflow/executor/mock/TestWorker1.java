package com.miotech.kun.workflow.executor.mock;

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

public class TestWorker1 implements Worker {

    private WorkflowExecutorFacade workflowExecutorFacade;

    private TaskAttemptMsg taskAttemptMsg;

    private final static Logger logger = LoggerFactory.getLogger(TestReconnectWorker.class);


    public TestWorker1(WorkflowExecutorFacade workflowExecutorFacade) {
        this.workflowExecutorFacade = workflowExecutorFacade;
    }

    @Override
    public void killTask(Boolean abort) {
        taskAttemptMsg.setWorkerId(1l);
        taskAttemptMsg.setTaskRunStatus(abort ? TaskRunStatus.ABORTED : TaskRunStatus.FAILED);
        taskAttemptMsg.setOperatorReport(OperatorReport.BLANK);
        workflowExecutorFacade.statusUpdate(taskAttemptMsg);
    }

    @Override
    public void start(ExecCommand command) {
        new Thread(new TestOperatorLaunch(command)).start();
    }

    @Override
    public boolean forceAbort() {
        return false;
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
                try {
                    logger.info("running worker send heartbeat");
                    message.setTimeoutTimes(0);
                    workflowExecutorFacade.heartBeat(message);
                    Thread.sleep(5000);
                } catch (Exception e) {

                }
            }
        }
    }

}
