package com.miotech.kun.workflow.worker.local;

import com.google.common.base.Joiner;
import com.miotech.kun.workflow.core.execution.ExecCommand;
import com.miotech.kun.workflow.core.execution.HeartBeatMessage;
import com.miotech.kun.workflow.facade.WorkflowWorkerFacade;
import com.miotech.kun.workflow.worker.JsonCodec;
import com.miotech.kun.workflow.worker.Worker;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.rpc.RpcContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;

public class LocalWorker implements Worker {

    private final Logger logger = LoggerFactory.getLogger(LocalWorker.class);


    private WorkflowWorkerFacade workerFacade;

    private Integer port;

    private Long processId;

    private Long taskAttemptId;

    @Inject
    public LocalWorker(WorkflowWorkerFacade workerFacade) {
        this.workerFacade = workerFacade;
    }


    @Override
    public void killTask(Boolean abort) {
        RpcContext.getContext().set("port", port);
        boolean result = workerFacade.killTask(abort);
        logger.info("kill task result = {}", result);
    }

    @Override
    public boolean shutdown() {
        logger.info("worker going to shutdown, taskAttemptId = {}", taskAttemptId);
        Runtime rt = Runtime.getRuntime();
        try {
            Process process;
            if (System.getProperty("os.name").toLowerCase().indexOf("windows") > -1)
                process = rt.exec("taskkill " + processId);
            else {
                process = rt.exec("kill -9 " + processId);
            }
            int exitCode = process.waitFor();
            return exitCode == 0 ? true : false;

        } catch (IOException | InterruptedException e) {
            logger.error("force kill worker failed processId = {} , taskAttemptId = {}", processId, taskAttemptId);
        }
        return false;

    }

    @Override
    public void start(ExecCommand command) {
        File inputFile;
        File configFile;
        File outputFile;
        File stdoutFile;
        HeartBeatMessage heartBeatMessage = new HeartBeatMessage();
        heartBeatMessage.setTaskAttemptId(command.getTaskAttemptId());
        try {
            inputFile = File.createTempFile("process_input", null);
            configFile = File.createTempFile("process_config", null);
            outputFile = File.createTempFile("process_output", null);
            stdoutFile = File.createTempFile("process_stdout", null);
            JsonCodec.MAPPER.writeValue(configFile, heartBeatMessage);
            JsonCodec.MAPPER.writeValue(inputFile, command);
        } catch (IOException e) {
            logger.error("Failed to create input/output file.", e);
            throw new IllegalStateException(e);
        }

        // 构建进程
        ProcessBuilder pb = new ProcessBuilder();
        pb.redirectErrorStream(true);
        pb.redirectOutput(stdoutFile);
        pb.command(buildCommand(inputFile.getPath(), configFile.getPath(), outputFile.getPath(), command.getTaskAttemptId()));
        String cmd = Joiner.on(" ").join(pb.command());
        logger.info("Start to run command: {}", cmd);

//         运行
        try {
            pb.start();
        } catch (IOException e) {
            logger.error("Failed to start process.", e);
            throw new IllegalStateException(e);
        }
    }

    public void bind(HeartBeatMessage heartBeatMessage) {
        this.processId = heartBeatMessage.getWorkerId();
        this.port = heartBeatMessage.getPort();
        this.taskAttemptId = taskAttemptId;
    }

    private List<String> buildCommand(String inputFile, String configFile, String outputFile, Long taskAttemptId) {
        List<String> command = new ArrayList<>();
        command.add("java");
        command.addAll(buildJVMArgs(taskAttemptId));
        command.add("-classpath");
        command.add(buildClassPath());
        command.add("com.miotech.kun.workflow.worker.local.OperatorLauncher");
        command.add(inputFile);
        command.add(configFile);
        command.add(outputFile);
        return command;
    }

    private String buildClassPath() {
        String classPath = System.getProperty("java.class.path");
        checkState(StringUtils.isNotEmpty(classPath), "launcher jar should exist.");
        return classPath;
    }

    private List<String> buildJVMArgs(Long taskAttemptId) {
        List<String> jvmArgs = new ArrayList<>();
        jvmArgs.add("-XX:+PrintGCDetails");
        jvmArgs.add("-XX:+HeapDumpOnOutOfMemoryError");
        jvmArgs.add(String.format("-XX:HeapDumpPath=/tmp/%d/heapdump.hprof", taskAttemptId));
        return jvmArgs;
    }
}
