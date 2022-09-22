package com.miotech.kun.workflow;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
public class TaskRunSMExecutor {

    private Logger logger = LoggerFactory.getLogger(TaskRunSMExecutor.class);
    private final TaskRunDao taskRunDao;
    private final TaskRunStateMachineBuffer buffer;
    private ExecutorService workers;


    @Inject
    public TaskRunSMExecutor(TaskRunDao taskRunDao, TaskRunStateMachineBuffer buffer) {
        this.taskRunDao = taskRunDao;
        this.buffer = buffer;
        init();
    }

    private void init() {
        workers = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("stateMache-worker" + "%d").build());
    }

    public void registerForExec(TaskRunStateMachine stateMachine) {
        if (stateMachine.setSchedule()) {
            StateMachineWorker worker = new StateMachineWorker(stateMachine);
            workers.submit(worker);
        }
    }

    private class StateMachineWorker implements Runnable {
        private TaskRunStateMachine stateMachine;

        public StateMachineWorker(TaskRunStateMachine stateMachine) {
            this.stateMachine = stateMachine;
        }

        @Override
        public void run() {
            stateMachine.doTransition();
        }
    }
}
