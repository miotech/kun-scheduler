package com.miotech.kun.workflow.common.dependency;

import com.google.inject.Inject;
import com.miotech.kun.metadata.facade.MetadataServiceFacade;
import com.miotech.kun.workflow.common.CommonTestBase;
import com.miotech.kun.workflow.common.operator.dao.OperatorDao;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.task.service.TaskService;
import com.miotech.kun.workflow.common.task.vo.TaskDependencyVO;
import com.miotech.kun.workflow.common.task.vo.TaskPropsVO;
import com.miotech.kun.workflow.core.Scheduler;
import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.testing.factory.MockOperatorFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import com.miotech.kun.workflow.testing.operator.NopOperator;
import com.miotech.kun.workflow.testing.operator.OperatorCompiler;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CircularDependencyTest extends CommonTestBase {


    @Inject
    private TaskService taskService;

    @Inject
    private TaskDao taskDao;

    @Inject
    private OperatorDao operatorDao;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Override
    protected boolean usePostgres() {
        return true;
    }

    @Override
    protected void configuration() {
        super.configuration();
        bind(MetadataServiceFacade.class,mock(MetadataServiceFacade.class));
        bind(Scheduler.class,mock(Scheduler.class));
    }

    @Test
    public void testFullUpdateTaskWithCircularDependencyShouldThrowsException(){
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(3,"0>>1;1>>2");
        Task task1 = taskList.get(0);
        Task task2 = taskList.get(1);
        Task task3 = taskList.get(2);
        taskDao.create(task1);
        taskDao.create(task2);
        taskDao.create(task3);

        //set circular dependency
        TaskDependencyVO updateDependency = TaskDependencyVO
                .newBuilder()
                .withDependencyFunc("latestTaskRun")
                .withUpstreamTaskId(task3.getId())
                .build();
        TaskPropsVO updateTask1 = TaskPropsVO.from(task1)
                .cloneBuilder().withDependencies(Arrays.asList(updateDependency))
                .build();
        long operatorId = task1.getOperatorId();
        Operator op = MockOperatorFactory.createOperator()
                .cloneBuilder()
                .withId(operatorId)
                .withName("Operator_" + operatorId)
                .withClassName("NopOperator")
                .withPackagePath(OperatorCompiler.compileJar(NopOperator.class, "NopOperator"))
                .build();
        operatorDao.createWithId(op, operatorId);
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("create task:" + task1.getId() + ", taskName:" + task1.getName()  + ",with circular dependencies:" +
                taskList.stream().map(Task::getId).sorted(Comparator.reverseOrder()).collect(Collectors.toList()));
        taskService.fullUpdateTaskById(task1.getId(),updateTask1);
    }

    @Test
    public void testPartialUpdateTaskWithCircularDependencyShouldThrowsException(){
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(3,"0>>1;1>>2");
        Task task1 = taskList.get(0);
        Task task2 = taskList.get(1);
        Task task3 = taskList.get(2);
        taskDao.create(task1);
        taskDao.create(task2);
        taskDao.create(task3);

        //set circular dependency
        TaskDependencyVO updateDependency = TaskDependencyVO
                .newBuilder()
                .withDependencyFunc("latestTaskRun")
                .withUpstreamTaskId(task3.getId())
                .build();
        TaskPropsVO updateTask1 = TaskPropsVO.from(task1)
                .cloneBuilder().withDependencies(Arrays.asList(updateDependency))
                .build();
        long operatorId = task1.getOperatorId();
        Operator op = MockOperatorFactory.createOperator()
                .cloneBuilder()
                .withId(operatorId)
                .withName("Operator_" + operatorId)
                .withClassName("NopOperator")
                .withPackagePath(OperatorCompiler.compileJar(NopOperator.class, "NopOperator"))
                .build();
        operatorDao.createWithId(op, operatorId);
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("create task:" + task1.getId() + ", taskName:" + task1.getName()  + ",with circular dependencies:" +
                taskList.stream().map(Task::getId).sorted(Comparator.reverseOrder()).collect(Collectors.toList()));
        taskService.partialUpdateTask(task1.getId(),updateTask1);
    }

    @Test
    public void testFullUpdateTaskWithCircularDependencyShouldSuccess(){
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(4,"0>>1;1>>2;1>>3");
        Task task1 = taskList.get(0);
        Task task2 = taskList.get(1);
        Task task3 = taskList.get(2);
        Task task4 = taskList.get(3);
        taskDao.create(task1);
        taskDao.create(task2);
        taskDao.create(task3);
        taskDao.create(task4);
        TaskDependencyVO updateDependency = TaskDependencyVO
                .newBuilder()
                .withDependencyFunc("latestTaskRun")
                .withUpstreamTaskId(task4.getId())
                .build();
        TaskPropsVO updateTask3 = TaskPropsVO.from(task3)
                .cloneBuilder().withDependencies(Arrays.asList(updateDependency))
                .build();
        long operatorId = task3.getOperatorId();
        Operator op = MockOperatorFactory.createOperator()
                .cloneBuilder()
                .withId(operatorId)
                .withName("Operator_" + operatorId)
                .withClassName("NopOperator")
                .withPackagePath(OperatorCompiler.compileJar(NopOperator.class, "NopOperator"))
                .build();
        operatorDao.createWithId(op, operatorId);
        taskService.fullUpdateTaskById(task3.getId(),updateTask3);
    }

    @Test
    public void testPartialUpdateTaskWithCircularDependencyShouldSuccess(){
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(4,"0>>1;1>>2;1>>3");
        Task task1 = taskList.get(0);
        Task task2 = taskList.get(1);
        Task task3 = taskList.get(2);
        Task task4 = taskList.get(3);
        taskDao.create(task1);
        taskDao.create(task2);
        taskDao.create(task3);
        taskDao.create(task4);
        TaskDependencyVO updateDependency = TaskDependencyVO
                .newBuilder()
                .withDependencyFunc("latestTaskRun")
                .withUpstreamTaskId(task4.getId())
                .build();
        TaskPropsVO updateTask3 = TaskPropsVO.from(task3)
                .cloneBuilder().withDependencies(Arrays.asList(updateDependency))
                .build();
        long operatorId = task3.getOperatorId();
        Operator op = MockOperatorFactory.createOperator()
                .cloneBuilder()
                .withId(operatorId)
                .withName("Operator_" + operatorId)
                .withClassName("NopOperator")
                .withPackagePath(OperatorCompiler.compileJar(NopOperator.class, "NopOperator"))
                .build();
        operatorDao.createWithId(op, operatorId);
        taskService.partialUpdateTask(task3.getId(),updateTask3);
    }
}
