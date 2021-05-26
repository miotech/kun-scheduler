package com.miotech.kun.workflow.web.service;

import com.miotech.kun.workflow.common.operator.service.OperatorService;
import com.miotech.kun.workflow.common.operator.vo.OperatorPropsVO;
import com.miotech.kun.workflow.common.task.filter.TaskSearchFilter;
import com.miotech.kun.workflow.common.task.service.TaskService;
import com.miotech.kun.workflow.common.task.vo.PaginationVO;
import com.miotech.kun.workflow.common.task.vo.RunTaskVO;
import com.miotech.kun.workflow.common.task.vo.TaskPropsVO;
import com.miotech.kun.workflow.common.taskrun.service.TaskRunService;
import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.facade.WorkflowServiceFacade;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public class WorkflowServiceFacadeImpl implements WorkflowServiceFacade {

    @Inject
    private TaskService taskService;

    @Inject
    private OperatorService operatorService;

    @Inject
    private TaskRunService taskRunService;

    @Override
    public Operator saveOperator(String name, Operator operator) {
        if (operatorService.fetchOperatorByName(name).isPresent()) {
            throw new IllegalArgumentException(String.format("Cannot create operator with duplicated name: \"%s\"", name));
        }
        return operatorService.saveOperator(operator);
    }

    @Override
    public Operator updateOperator(Long operatorId, Operator operator) {
        OperatorPropsVO operatorPropsVO = OperatorPropsVO.newBuilder()
                .withClassName(operator.getClassName())
                .withDescription(operator.getDescription())
                .withName(operator.getName())
                .build();
        return operatorService.fullUpdateOperator(operatorId, operatorPropsVO);
    }

    @Override
    public List<Operator> getOperators() {
        return operatorService.fetchAllOperators();
    }

    @Override
    public void uploadOperatorJar(Long operatorId, File jarFile) {
        FileItem fileItem = createFileItem(jarFile, jarFile.getName());
        operatorService.uploadOperatorJar(operatorId, Arrays.asList(fileItem));
    }

    @Override
    public Optional<Operator> getOperator(String operatorName) {
        return operatorService.fetchOperatorByName(operatorName);
    }

    @Override
    public Optional<Task> getTask(String taskName) {
        TaskSearchFilter filter = TaskSearchFilter.newBuilder()
                .withName(taskName).build();
        PaginationVO<Task> paginationVO = taskService.fetchTasksByFilters(filter);
        return paginationVO.getRecords().stream().filter(x -> x.getName().equals(taskName)).findAny();
    }

    @Override
    public Task createTask(Task task) {
        TaskPropsVO vo = TaskPropsVO.from(task);
        return taskService.createTask(vo);
    }

    @Override
    public TaskRun executeTask(Long taskId, Map<String, Object> taskConfig) {
        RunTaskVO runTaskVO = new RunTaskVO();
        runTaskVO.setTaskId(taskId);
        runTaskVO.setConfig(taskConfig);
        List<Long> taskRunIds = taskService.runTasks(Arrays.asList(runTaskVO));
        if (taskRunIds.size() > 0) {
            return taskRunService.findTaskRun(taskRunIds.get(0));
        }
        return null;
    }

    @Override
    public TaskRun getTaskRun(Long taskRunId) {
        return taskRunService.findTaskRun(taskRunId);
    }

    private FileItem createFileItem(File file, String fieldName) {
        FileItemFactory factory = new DiskFileItemFactory(16, null);
        FileItem item = factory.createItem(fieldName, "text/plain", true, file.getName());
        int bytesRead = 0;
        byte[] buffer = new byte[8192];
        try {
            FileInputStream fis = new FileInputStream(file);
            OutputStream os = item.getOutputStream();
            while ((bytesRead = fis.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return item;
    }
}
