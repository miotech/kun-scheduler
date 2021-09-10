package com.miotech.kun.workflow.executor.local;

import com.google.common.base.Joiner;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.workflow.core.execution.ExecCommand;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.core.model.worker.DatabaseConfig;
import com.miotech.kun.workflow.core.model.worker.WorkerInstance;
import com.miotech.kun.workflow.core.model.worker.WorkerInstanceKind;
import com.miotech.kun.workflow.executor.local.model.LocalProcessParams;
import com.miotech.kun.workflow.worker.JsonCodec;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static com.google.common.base.Preconditions.checkState;
import static com.miotech.kun.workflow.executor.local.LocalProcessConstants.KUN_ATTEMPT_ID;
import static com.miotech.kun.workflow.executor.local.LocalProcessConstants.KUN_QUEUE_NAME;

@Singleton
public class LocalProcessBackend {

    private static final Logger logger = LoggerFactory.getLogger(LocalProcessBackend.class);
    private BlockingQueue<ProcessSnapShot> processSnapShotQueue = new LinkedBlockingQueue<>();
    private final String THREAD_NAME_PREFIX = "processMonitor";
    private ThreadPoolExecutor processPool = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
            new ThreadFactoryBuilder().setNameFormat(THREAD_NAME_PREFIX +"-%d").build());

    public Process startProcess(ExecCommand command, DatabaseConfig databaseConfig) {
        File inputFile;
        File configFile;
        File outputFile;
        File stdoutFile;
        try {
            inputFile = File.createTempFile("process_input", null);
            configFile = File.createTempFile("process_config", null);
            outputFile = File.createTempFile("process_output", null);
            stdoutFile = File.createTempFile("process_stdout", null);
            JsonCodec.MAPPER.writeValue(inputFile, command);
            JsonCodec.MAPPER.writeValue(configFile, databaseConfig);
        } catch (IOException e) {
            logger.error("Failed to create input/output file.", e);
            throw new IllegalStateException(e);
        }

        // 构建进程
        ProcessBuilder pb = new ProcessBuilder();
        pb.redirectErrorStream(true);
        pb.redirectOutput(stdoutFile);
        pb.command(buildCommand(inputFile.getPath(), configFile.getPath(), outputFile.getPath(), command));
        String cmd = Joiner.on(" ").join(pb.command());
        logger.info("Start to run command: {}", cmd);
//         运行
        try {
            Process process = pb.start();
            ProcessMonitorTask processMonitorTask = new ProcessMonitorTask(command.getTaskAttemptId(), process);
            processPool.submit(processMonitorTask);
            return process;
        } catch (IOException e) {
            logger.error("Failed to start process.", e);
            throw new IllegalStateException(e);
        }

    }


    /**
     * register watcher to watch running process
     * The onReceiveSnapshot method is called when the process status is updated
     *
     * @param processWatcher
     */
    public void watch(ProcessWatcher processWatcher) {
        //start a thread to poll snapshot from queue
        logger.info("start process watcher...");
        Thread processWatcherTask = new Thread(new ProcessWatcherTask(processWatcher), "processWatcherThread");
        processWatcherTask.start();
    }

    public List<ProcessSnapShot> fetchRunningProcess() {
        return fetchRunningProcess(new LocalProcessParams());
    }

    public ProcessSnapShot fetchProcessByTaskAttemptId(Long taskAttemptId) {
        LocalProcessParams localProcessParams = new LocalProcessParams();
        localProcessParams.setTaskAttemptId(taskAttemptId);
        List<ProcessSnapShot> runningProcess = fetchRunningProcess(localProcessParams);
        if (runningProcess.size() == 0) {
            return null;
        }
        return runningProcess.get(0);
    }

    public List<ProcessSnapShot> fetchRunningProcess(String queueName) {
        LocalProcessParams localProcessParams = new LocalProcessParams();
        localProcessParams.setQueueName(queueName);
        return fetchRunningProcess(localProcessParams);
    }

    public List<ProcessSnapShot> fetchRunningProcess(LocalProcessParams localProcessParams) {
        List<String> cmds = new ArrayList<>();
        List<ProcessSnapShot> runningProcess = new ArrayList<>();
        try {
            cmds.add("sh");
            cmds.add("-c");
            StringBuilder queryCommand = new StringBuilder();
            queryCommand.append("jps -v | grep " + KUN_ATTEMPT_ID + "=");
            if (localProcessParams.getTaskAttemptId() != null) {
                queryCommand.append(localProcessParams.getTaskAttemptId());
            }
            if (localProcessParams.getQueueName() != null) {
                queryCommand.append("|grep " + KUN_QUEUE_NAME + "=" + localProcessParams.getQueueName());
            }
            cmds.add(queryCommand.toString());
            ProcessBuilder processBuilder = new ProcessBuilder(cmds);
            Process process = processBuilder.start();
            InputStream fis = process.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line;
            List<String> outputList = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                outputList.add(line);
            }
            for (String output : outputList) {
                logger.debug("found running process message : {}",output);
                Long taskAttemptId = findTaskAttemptId(output);
                WorkerInstance workerInstance = new WorkerInstance(taskAttemptId, findPid(output), "local", WorkerInstanceKind.LOCAL_PROCESS);
                ProcessSnapShot processSnapShot = new ProcessSnapShot(workerInstance, TaskRunStatus.RUNNING);
                runningProcess.add(processSnapShot);
            }

        } catch (IOException e) {
            ExceptionUtils.wrapIfChecked(e);
        }
        return runningProcess;

    }

    public void stopProcess(Long taskAttemptId){
        ProcessSnapShot processSnapShot = fetchProcessByTaskAttemptId(taskAttemptId);
        if (processSnapShot == null) {
            return ;
        }
        String processId = processSnapShot.getIns().getWorkerId();

        logger.info("worker going to shutdown, taskAttemptId = {},processId ={}", taskAttemptId, processId);
        Runtime rt = Runtime.getRuntime();
        try {
            if (System.getProperty("os.name").toLowerCase().indexOf("windows") > -1)
                rt.exec("taskkill " + processId);
            else {
                rt.exec("kill -15 " + processId);
            }

        } catch (IOException e) {
            logger.error("kill worker failed processId = {} , taskAttemptId = {}", processId, taskAttemptId);
        }
    }

    public void forceStopProcess(Long taskAttemptId){
        ProcessSnapShot processSnapShot = fetchProcessByTaskAttemptId(taskAttemptId);
        if (processSnapShot == null) {
            return ;
        }
        String processId = processSnapShot.getIns().getWorkerId();

        logger.info("going to force kill worker, taskAttemptId = {},processId ={}", taskAttemptId, processId);
        Runtime rt = Runtime.getRuntime();
        try {
            if (System.getProperty("os.name").toLowerCase().indexOf("windows") > -1)
                rt.exec("taskkill " + processId);
            else {
                rt.exec("kill -9 " + processId);
            }

        } catch (IOException e) {
            logger.error("force kill worker failed processId = {} , taskAttemptId = {}", processId, taskAttemptId);
        }
    }

    private Long findTaskAttemptId(String output) {
        String match = "Dkun.taskAttemptId=";
        int index = output.indexOf("Dkun.taskAttemptId=");
        String[] strs = output.substring(index + match.length()).split(" ");
        return Long.valueOf(strs[0]);
    }

    private String findPid(String output) {
        String[] strs = output.split(" ");
        return strs[0];
    }

    private List<String> buildCommand(String inputFile, String configFile, String outputFile, ExecCommand execCommand) {
        List<String> command = new ArrayList<>();
        command.add("java");
        command.addAll(buildJVMArgs(execCommand.getTaskAttemptId()));
        //add params to recognize process
        command.add("-" + KUN_ATTEMPT_ID + "=" + execCommand.getTaskAttemptId());
        command.add("-" + KUN_QUEUE_NAME + "=" + execCommand.getQueueName());
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

    class ProcessMonitorTask implements Runnable {

        private Process process;
        private Long taskAttemptId;

        public ProcessMonitorTask(Long taskAttemptId, Process process) {
            this.process = process;
            this.taskAttemptId = taskAttemptId;
        }

        @Override
        public void run() {
            WorkerInstance workerInstance = new WorkerInstance(taskAttemptId, "", "local", WorkerInstanceKind.LOCAL_PROCESS);
            ProcessSnapShot processSnapShot = new ProcessSnapShot(workerInstance, TaskRunStatus.RUNNING);
            processSnapShotQueue.add(processSnapShot);
            int exitCode = 1;
            try {
                exitCode = process.waitFor();
            } catch (InterruptedException e) {
                logger.warn("process monitor thread is interrupted");
            }
            workerInstance = new WorkerInstance(taskAttemptId, "", "local", WorkerInstanceKind.LOCAL_PROCESS);
            TaskRunStatus taskRunStatus;
            switch (exitCode) {
                case 0:
                    taskRunStatus = TaskRunStatus.SUCCESS;
                    break;
                case 1:
                    taskRunStatus = TaskRunStatus.FAILED;
                    break;
                default:
                    //ignore other exit code
                    return;
            }
            processSnapShot = new ProcessSnapShot(workerInstance, taskRunStatus);
            processSnapShotQueue.add(processSnapShot);
        }
    }

    class ProcessWatcherTask implements Runnable {

        private final ProcessWatcher processWatcher;

        public ProcessWatcherTask(ProcessWatcher processWatcher) {
            this.processWatcher = processWatcher;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    ProcessSnapShot processSnapShot = processSnapShotQueue.take();
                    processWatcher.eventReceived(processSnapShot);
                } catch (Throwable e) {
                    logger.error("handle processSnapShot failed", e);
                }
            }
        }
    }
}
