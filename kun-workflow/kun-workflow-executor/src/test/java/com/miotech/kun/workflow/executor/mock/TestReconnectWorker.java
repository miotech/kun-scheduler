package com.miotech.kun.workflow.executor.mock;

import com.miotech.kun.workflow.core.execution.ExecCommand;
import com.miotech.kun.workflow.core.execution.HeartBeatMessage;
import com.miotech.kun.workflow.core.execution.OperatorReport;
import com.miotech.kun.workflow.core.execution.TaskAttemptMsg;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.facade.WorkflowExecutorFacade;
import com.miotech.kun.workflow.worker.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestReconnectWorker implements Worker {

    private WorkflowExecutorFacade workflowExecutorFacade;

    private TaskAttemptMsg taskAttemptMsg;

    private final static Logger logger = LoggerFactory.getLogger(TestReconnectWorker.class);


    public TestReconnectWorker(WorkflowExecutorFacade workflowExecutorFacade){
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
    public boolean shutdown() {
        return true;
    }

    class TestOperatorLaunch implements Runnable{

        private ExecCommand command;

        public TestOperatorLaunch(ExecCommand command){
            this.command = command;
        }

        @Override
        public void run() {
            try {
                logger.info("init worker ....");
                Thread.sleep(1000);
            }catch (Exception e){

            }

            taskAttemptMsg = new TaskAttemptMsg();
            taskAttemptMsg.setTaskAttemptId(command.getTaskAttemptId());
            HeartBeatMessage message = new HeartBeatMessage();
            message.setWorkerId(1l);
            message.setTaskAttemptId(command.getTaskAttemptId());
            message.setTaskRunId(command.getTaskRunId());
            message.setPort(11111);
            message.setTaskRunStatus(TaskRunStatus.RUNNING);
            workflowExecutorFacade.heartBeat(message);
            try {
                logger.info("test worker has disconnected");
                Thread.sleep(20000);
            }catch (Exception e){

            }
            message.setTimeoutTimes(0);
            logger.info("test worker has reconnect");
            workflowExecutorFacade.heartBeat(message);

        }
    }
}
