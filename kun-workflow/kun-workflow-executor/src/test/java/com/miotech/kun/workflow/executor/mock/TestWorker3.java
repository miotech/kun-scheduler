package com.miotech.kun.workflow.executor.mock;

import com.miotech.kun.workflow.core.execution.ExecCommand;
import com.miotech.kun.workflow.core.execution.TaskAttemptMsg;
import com.miotech.kun.workflow.facade.WorkflowExecutorFacade;
import com.miotech.kun.workflow.worker.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestWorker3 implements Worker {

    private WorkflowExecutorFacade workflowExecutorFacade;

    private TaskAttemptMsg taskAttemptMsg;

    private static volatile boolean exit = false;

    private final static Logger logger = LoggerFactory.getLogger(TestWorker3.class);


    public TestWorker3(WorkflowExecutorFacade workflowExecutorFacade) {
        this.workflowExecutorFacade = workflowExecutorFacade;
    }

    @Override
    public void killTask(Boolean abort) {
        logger.info("worker going to shutdown");
        exit = true;
    }

    @Override
    public void start(ExecCommand command) {
        new Thread(new TestWorker3.TestOperatorLaunch(command)).start();
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
            try {
                logger.info("init worker ....");
                Thread.sleep(1000);
            } catch (Exception e) {

            }
            logger.info("START RUNNING");
        }
    }

}
