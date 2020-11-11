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

public class TestTimeoutWorker implements Worker {

    private WorkflowExecutorFacade workflowExecutorFacade;

    private TaskAttemptMsg taskAttemptMsg;

    private final static Logger logger = LoggerFactory.getLogger(TestReconnectWorker.class);


    public TestTimeoutWorker(WorkflowExecutorFacade workflowExecutorFacade){
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
            message.setTaskRunStatus(TaskRunStatus.RUNNING);
            message.setPort(11111);
            workflowExecutorFacade.heartBeat(message);
            try {
                logger.info("test worker has time out");
                Thread.sleep(20000);
            }catch (Exception e){

            }

        }
    }

}
