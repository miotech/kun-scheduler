package com.miotech.kun.workflow.executor.storage;

import com.google.common.base.Strings;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.workflow.common.resource.ResourceLoader;
import com.miotech.kun.workflow.common.resource.ResourceLoaderImpl;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.StorageManager;
import com.miotech.kun.workflow.core.annotation.Internal;
import com.miotech.kun.workflow.core.execution.ExecCommand;
import com.miotech.kun.workflow.core.model.WorkerLogs;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.resource.Resource;
import com.miotech.kun.workflow.worker.JsonCodec;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LocalStorageManager implements StorageManager {

    private final Logger logger = LoggerFactory.getLogger(LocalStorageManager.class);
    private ResourceLoader resourceLoader;
    private final TaskRunDao taskRunDao;
    private String commandDir = "";
    private String logDir = "";
    private String operatorDir = "";

    public LocalStorageManager(TaskRunDao taskRunDao) {
        this.taskRunDao = taskRunDao;
    }

    @Override
    public void init(Map<String, String> storageConfig) {
        resourceLoader = new ResourceLoaderImpl();
        commandDir = storageConfig.get("commandDir");
        logDir = storageConfig.get("logDir");
        operatorDir = storageConfig.get("operatorDir");
        logger.debug("init commandDir = {}, logDir = {}, operatorDir = {}", commandDir, logDir, operatorDir);
        if (Strings.isNullOrEmpty(commandDir) || Strings.isNullOrEmpty(logDir) || Strings.isNullOrEmpty(operatorDir)) {
            throw new IllegalArgumentException("commandDir,logDir,operatorDir can not be null");
        }
        createDirsIfNotExist();
    }

    @Override
    public void uploadOperator(Long operatorId, String sourceName) {
        File source = new File(sourceName);
        String directName = operatorDir + "/" + operatorId;
        String operatorName = sourceName.substring(sourceName.lastIndexOf("/") + 1);
        File direct = new File(directName);
        try {
            if (!direct.exists()) {
                direct.mkdirs();
            }
            File target = new File(directName + "/" + operatorName);
            Files.copy(source.toPath(), target.toPath());
        } catch (IOException e) {
            logger.error("copy operator to operator lib failed", e);
        }
    }

    @Override
    public ExecCommand writeExecCommand(ExecCommand execCommand) {
        String filePath = commandDir + "/" + execCommand.getTaskAttemptId();
        try {
            File execCommandFile = new File(filePath);
            JsonCodec.MAPPER.writeValue(execCommandFile, execCommand);
        } catch (IOException e) {
            logger.error("failed to write exec command to file = {} ", filePath, e);
        }
        return execCommand;
    }

    @Override
    public WorkerLogs workerLog(Long taskAttemptId, Integer startLine, Integer endLine) {
        Resource resource = null;
        Integer lineCount = 0;
        TaskAttempt taskAttempt = taskRunDao.fetchAttemptById(taskAttemptId).get();
        String logPath = covertLogPath(taskAttempt.getLogPath());
        logger.debug("read log from logPath:{}", logPath);
        try {
            resource = resourceLoader.getResource(logPath);
            lineCount = getLineCountOfFile(resource);
        } catch (RuntimeException e) {
            logger.warn("Cannot find or open log path for existing task attempt: {}", logPath);
            return new WorkerLogs(new ArrayList<>(), 0, 0, 0);
        }

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            Triple<List<String>, Integer, Integer> logsTriple = readLinesFromLogFile(bufferedReader, lineCount, startLine, endLine);
            return new WorkerLogs(logsTriple.getLeft(), logsTriple.getMiddle(), logsTriple.getRight(), lineCount);
        } catch (IOException e) {
            logger.error("Failed to get task attempt log: {}", taskAttempt.getLogPath(), e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    /**
     * Read lines of a log file in specific range.
     *
     * @param reader         buffered reader instance
     * @param totalLineCount total line count of that log file
     * @param startLineIndex Index of start line. Allows negative indexes.
     * @param endLineIndex   Index of stop line. Allows negative indexes.
     * @return Triple of (log lines, actual start line index, actual end line index)
     * @throws IOException if log file not found, or other IO exception cases
     */
    @Internal
    public Triple<List<String>, Integer, Integer> readLinesFromLogFile(BufferedReader reader, int totalLineCount, Integer startLineIndex, Integer endLineIndex) throws IOException {
        int startLineActual = (startLineIndex == null) ? 0 : ((startLineIndex >= 0) ? startLineIndex : totalLineCount + startLineIndex);
        int endLineActual = (endLineIndex == null) ? Integer.MAX_VALUE : ((endLineIndex >= 0) ? endLineIndex : totalLineCount + endLineIndex);

        String line;
        List<String> logs = new ArrayList<>();

        int i = 0;
        for (; (i < endLineActual) && (line = reader.readLine()) != null; i++) {
            if (i >= startLineActual) {
                logs.add(line);
            }
        }
        // Triple of (logs, actual start line index, actual end line index)
        return Triple.of(logs, startLineActual, startLineActual + logs.size() - 1);
    }

    /**
     * Get line count of a resource
     *
     * @param fileResource resource instance
     * @return An non-negative integer of total file line count.
     */
    @Internal
    private int getLineCountOfFile(Resource fileResource) {
        int lineCount;
        try (LineNumberReader lineNumberReader = new LineNumberReader(new InputStreamReader(fileResource.getInputStream()))) {
            while (null != lineNumberReader.readLine()) ;  // loop until EOF
            lineCount = lineNumberReader.getLineNumber();
        } catch (IOException e) {
            logger.error("Failed to create line number reader for resource: {}", fileResource);
            throw ExceptionUtils.wrapIfChecked(e);
        }
        return lineCount;
    }

    private String covertLogPath(String attemptLogPath) {
        logger.debug("covert attempt logPath {}", attemptLogPath);
        Integer suffixIndex = attemptLogPath.lastIndexOf("/",attemptLogPath.lastIndexOf("/") - 1);        //suffix contains /
        String suffix = attemptLogPath.substring(suffixIndex);
        return "file:" + logDir + suffix;
    }

    private void createDirsIfNotExist() {
        File logDirectory = new File(logDir);
        File commandDirectory = new File(commandDir);
        File operatorDirectory = new File(operatorDir);
        if (!logDirectory.exists()) {
            logDirectory.mkdirs();
        }
        if (!commandDirectory.exists()) {
            commandDirectory.mkdirs();
        }
        if (!operatorDirectory.exists()) {
            operatorDirectory.mkdirs();
        }
    }
}
