package com.miotech.kun.workflow.executor.local.process;

import com.google.common.base.Joiner;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.workflow.core.execution.TaskAttemptReport;
import com.miotech.kun.workflow.executor.ExecCommand;
import com.miotech.kun.workflow.executor.ExecResult;
import com.miotech.kun.workflow.executor.JsonCodec;
import com.miotech.kun.workflow.executor.TaskRunner;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkState;

public class ProcessTaskRunner implements TaskRunner {
    private static final Logger logger = LoggerFactory.getLogger(ProcessTaskRunner.class);

    private static final ListeningExecutorService EXECUTOR = MoreExecutors.listeningDecorator(
            new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue<>(),
                    new ThreadFactoryBuilder().setDaemon(true).setNameFormat("process-task-executor-waiter-%d").build()));

    public static final String SIG_TERM = "SIGTERM";

    private volatile Process process = null;
    private volatile boolean forceAborted;

    @Override
    public ListenableFuture<ExecResult> run(ExecCommand command) {
        // 初始化
        File inputFile;
        File outputFile;
        File stdoutFile;
        try {
            inputFile = File.createTempFile("process_input", null);
            outputFile = File.createTempFile("process_output", null);
            stdoutFile = File.createTempFile("process_stdout", null);
            JsonCodec.MAPPER.writeValue(inputFile, command);
        } catch (IOException e) {
            logger.error("Failed to create input/output file.", e);
            throw new IllegalStateException(e);
        }

        // 构建进程
        ProcessBuilder pb = new ProcessBuilder();
        pb.redirectErrorStream(true);
        pb.redirectOutput(stdoutFile);
        pb.command(buildCommand(inputFile.getPath(), outputFile.getPath()));
        String cmd = Joiner.on(" ").join(pb.command());
        logger.info("Start to run command: {}", cmd);

        // 运行
        try {
            process = pb.start();
        } catch (IOException e) {
            logger.error("Failed to start process.", e);
            throw new IllegalStateException(e);
        }

        ListenableFuture<ExecResult> runFuture = EXECUTOR.submit(() -> {
            int exitCode = process.waitFor();
            logger.debug("Command: {}, Exit code: {}", cmd, exitCode);

            logAndDelete(stdoutFile);
            if (forceAborted) {
                return buildResultAfterForceAbort();
            } else {
                return buildResult(outputFile);
            }
        });

        return runFuture;
    }

    @Override
    public void abort() {
        try {
            logger.debug("Try to send term signal.");
            process.getOutputStream().write(SIG_TERM.getBytes());
            process.getOutputStream().flush();
        } catch (IOException e) {
            logger.error("Failed to write signal.");
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    @Override
    public void forceAbort() {
        if (forceAborted) {
            return;
        }
        forceAborted = true;
        process.destroyForcibly();
    }

    private List<String> buildCommand(String inputFile, String outputFile) {
        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-classpath");
        command.add(buildClassPath());
        command.add(OperatorLauncher.class.getName());
        command.add(inputFile);
        command.add(outputFile);
        return command;
    }

    private String buildClassPath() {
        String classPath = System.getProperty("java.class.path");
        checkState(StringUtils.isNotEmpty(classPath), "launcher jar should exist.");
        return classPath;
    }

    private ExecResult buildResult(File outputFile) {
        try {
            return JsonCodec.MAPPER.readValue(outputFile, ExecResult.class);
        } catch (IOException e) {
            logger.error("Failed to parse result.", e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    private ExecResult buildResultAfterForceAbort() {
        ExecResult res = new ExecResult();
        res.setSuccess(false);
        res.setCancelled(true);
        res.setReport(TaskAttemptReport.BLANK);
        return res;
    }

    private void logAndDelete(File file) {
        if (logger.isDebugEnabled()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
                String line;
                logger.debug("The following is the log output by the Operator child process!");
                while ((line = reader.readLine()) != null) {
                    logger.debug(line);
                }

                logger.debug("The above is the log output by the Operator child process!");
                Files.delete(file.toPath());
            } catch (IOException e) {
                // ignore
            }
        }
    }
}
