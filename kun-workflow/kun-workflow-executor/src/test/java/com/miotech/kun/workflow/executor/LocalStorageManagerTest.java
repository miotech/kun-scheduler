package com.miotech.kun.workflow.executor;

import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.workflow.common.resource.ResourceLoader;
import com.miotech.kun.workflow.common.resource.ResourceLoaderImpl;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.execution.ExecCommand;
import com.miotech.kun.workflow.core.model.WorkerLogs;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.resource.Resource;
import com.miotech.kun.workflow.executor.storage.LocalStorageManager;
import com.miotech.kun.workflow.executor.storage.StorageManagerFactory;
import com.miotech.kun.workflow.executor.storage.StorageType;
import com.miotech.kun.workflow.testing.factory.MockTaskAttemptFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskRunFactory;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import com.miotech.kun.workflow.worker.JsonCodec;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class LocalStorageManagerTest extends DatabaseTestBase {

    @Inject
    private StorageManagerFactory storageManagerFactory;

    @Inject
    private TaskRunDao taskRunDao;

    @Inject
    private TaskDao taskDao;

    private LocalStorageManager localStorageManager;

    private ResourceLoader resourceLoader = new ResourceLoaderImpl();

    @BeforeEach
    public void init() {
        localStorageManager = (LocalStorageManager) storageManagerFactory.createStorageManager(StorageType.LOCAL);
        Map<String, String> storageMap = new HashMap<>();
        storageMap.put("logDir", "/tmp/logs");
        storageMap.put("operatorDir", "/tmp/operators");
        storageMap.put("commandDir", "/tmp/commands");
        localStorageManager.init(storageMap);
    }

    @AfterEach
    public void cleanUp() {
        deleteFiles(new File("/tmp/logs"));
        deleteFiles(new File("/tmp/operators"));
        deleteFiles(new File("/tmp/commands"));
    }


    @Test
    public void uploadOperatorTest() throws IOException {
        //prepare
        File operator = new File("/tmp/testOperator.jar");
        operator.createNewFile();
        Long operatorId = IdGenerator.getInstance().nextId();

        localStorageManager.uploadOperator(operatorId, "/tmp/testOperator.jar");

        //verify
        File updatedOperator = new File("/tmp/operators/" + operatorId + "/testOperator.jar");
        assertTrue(updatedOperator.exists());
        operator.delete();
    }

    @Test
    public void writeCommandTest() throws IOException {
        //prepare
        ExecCommand execCommand = new ExecCommand();
        Long taskAttemptId = IdGenerator.getInstance().nextId();
        execCommand.setTaskAttemptId(taskAttemptId);
        localStorageManager.writeExecCommand(execCommand);

        File execCommandFile = new File("/tmp/commands/" + taskAttemptId);
        ExecCommand wroteCommand = JsonCodec.MAPPER.readValue(execCommandFile, ExecCommand.class);

        //verify
        assertThat(wroteCommand.getTaskAttemptId(), is(taskAttemptId));
    }

    @Test
    public void getWorkerLogTest() throws IOException{
        //prepare
        Task task = MockTaskFactory.createTask();
        TaskRun taskRun = MockTaskRunFactory.createTaskRun(task);
        Long taskAttemptId = WorkflowIdGenerator.nextTaskAttemptId(taskRun.getId(), 1);
        TaskAttempt taskAttempt = MockTaskAttemptFactory.createTaskAttempt(taskRun)
                .cloneBuilder()
                .withLogPath("file:logs/20100101/" + taskAttemptId)
                .build();
        taskDao.create(task);
        taskRunDao.createTaskRun(taskRun);
        taskRunDao.createAttempt(taskAttempt);
        String testStr = "hellow world";
        String attemptLogPath = "/tmp/logs/20100101/" + taskAttemptId;
        createResource(attemptLogPath, testStr, 2);
        WorkerLogs workerLogs = localStorageManager.workerLog(taskAttemptId,0,3);

        assertThat(workerLogs.getStartLine(),is(0));
        assertThat(workerLogs.getLineCount(),is(2));
        assertThat(workerLogs.getLogs(),hasSize(2));

    }

    private void deleteFiles(File file) {
        if (!file.exists()) {
            return;
        } else if (file.isFile()) {
            file.delete();
        } else {
            String[] childFilePath = file.list();
            for (String path : childFilePath) {
                File childFile = new File(file.getAbsoluteFile() + "/" + path);
                deleteFiles(childFile);
            }
            file.delete();
        }
    }

    private Resource createResource(String fileName, String text, int times) throws IOException {
        File file = new File(fileName);
        File parent = file.getParentFile();
        if(!parent.exists()){
            parent.mkdirs();
        }
        file.createNewFile();
        Resource resource = resourceLoader.getResource("file://" + file.getPath(), true);
        Writer writer = new PrintWriter(resource.getOutputStream());

        for (int i = 0; i < times; i++) {
            writer.write(text + "\n");
        }
        writer.flush();
        return resource;
    }

}
