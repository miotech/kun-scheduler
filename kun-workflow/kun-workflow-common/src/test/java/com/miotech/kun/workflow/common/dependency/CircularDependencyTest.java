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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
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
        TaskPropsVO updateTask1 = updateForAddingDeps(task1,Arrays.asList(task3.getId()));
        long operatorId = task1.getOperatorId();
        Operator op = MockOperatorFactory.createOperatorWithId(operatorId);
        operatorDao.createWithId(op, operatorId);
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("create task:" + task1.getId() + ", taskName:" + task1.getName()  + ",with circular dependencies:" +
                taskList.stream().map(Task::getId).sorted(Comparator.reverseOrder()).collect(Collectors.toList()));
        taskService.fullUpdateTaskById(task1.getId(),updateTask1);
    }

    @Test
    public void testFullUpdateTaskWithSelfDependencyShouldThrowsException(){
        Task task = MockTaskFactory.createTask();
        taskDao.create(task);

        //set circular dependency
        TaskPropsVO updateTask1 = updateForAddingDeps(task,Arrays.asList(task.getId()));
        long operatorId = task.getOperatorId();
        Operator op = MockOperatorFactory.createOperatorWithId(operatorId);
        operatorDao.createWithId(op, operatorId);
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("create task:" + task.getId() + ", taskName:" + task.getName()  + ",with circular dependencies:" +
                Arrays.asList(task.getId()));
        taskService.fullUpdateTaskById(task.getId(),updateTask1);
    }

    @Test
    public void testUpdateTaskWithCircularDependencyShouldThrowsException(){
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(2,"0>>1");
        Task task1 = taskList.get(0);
        Task task2 = taskList.get(1);
        taskDao.create(task1);
        taskDao.create(task2);

        //set circular dependency
        TaskPropsVO updateTask1 = updateForAddingDeps(task1,Arrays.asList(task2.getId()));
        long operatorId = task1.getOperatorId();
        Operator op = MockOperatorFactory.createOperatorWithId(operatorId);
        operatorDao.createWithId(op, operatorId);
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("create task:" + task1.getId() + ", taskName:" + task1.getName()  + ",with circular dependencies:" +
                taskList.stream().map(Task::getId).sorted(Comparator.reverseOrder()).collect(Collectors.toList()));
        taskService.partialUpdateTask(task1.getId(),updateTask1);
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
        TaskPropsVO updateTask1 = updateForAddingDeps(task1,Arrays.asList(task3.getId()));
        long operatorId = task1.getOperatorId();
        Operator op = MockOperatorFactory.createOperatorWithId(operatorId);
        operatorDao.createWithId(op, operatorId);
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("create task:" + task1.getId() + ", taskName:" + task1.getName()  + ",with circular dependencies:" +
                taskList.stream().map(Task::getId).sorted(Comparator.reverseOrder()).collect(Collectors.toList()));
        taskService.partialUpdateTask(task1.getId(),updateTask1);
    }

    @Test
    public void testFullUpdateTaskWithoutCircularDependencyShouldSuccess(){
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(4,"0>>1;1>>2;1>>3");
        Task task1 = taskList.get(0);
        Task task2 = taskList.get(1);
        Task task3 = taskList.get(2);
        Task task4 = taskList.get(3);
        taskDao.create(task1);
        taskDao.create(task2);
        taskDao.create(task3);
        taskDao.create(task4);
        TaskPropsVO updateTask3 = updateForAddingDeps(task3,Arrays.asList(task4.getId()));
        long operatorId = task3.getOperatorId();
        Operator op = MockOperatorFactory.createOperatorWithId(operatorId);
        operatorDao.createWithId(op, operatorId);
        taskService.fullUpdateTaskById(task3.getId(),updateTask3);
    }

    @Test
    public void testPartialUpdateTaskWithoutCircularDependencyShouldSuccess(){
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(4,"0>>1;1>>2;1>>3");
        Task task1 = taskList.get(0);
        Task task2 = taskList.get(1);
        Task task3 = taskList.get(2);
        Task task4 = taskList.get(3);
        taskDao.create(task1);
        taskDao.create(task2);
        taskDao.create(task3);
        taskDao.create(task4);
        TaskPropsVO updateTask3 = updateForAddingDeps(task3,Arrays.asList(task4.getId()));
        long operatorId = task3.getOperatorId();
        Operator op = MockOperatorFactory.createOperatorWithId(operatorId);
        operatorDao.createWithId(op, operatorId);
        taskService.partialUpdateTask(task3.getId(),updateTask3);
    }

    private TaskPropsVO updateForAddingDeps(Task toUpdate,List<Long> dependTaskIds){
        List<TaskDependencyVO> dependencyList = new ArrayList<>();
        for(Long dependTaskId : dependTaskIds){
            TaskDependencyVO updateDependency = TaskDependencyVO
                    .newBuilder()
                    .withDependencyFunc("latestTaskRun")
                    .withUpstreamTaskId(dependTaskId)
                    .build();
            dependencyList.add(updateDependency);
        }
        return TaskPropsVO.from(toUpdate)
                .cloneBuilder().withDependencies(dependencyList)
                .build();

    }
}
