package com.miotech.kun.workflow.testing.factory;

import com.miotech.kun.workflow.core.model.task.Task;

import java.util.ArrayList;
import java.util.List;

public class MockTaskFactory {
    public static Task createTask() {
        return createTasks(1).get(0);
    }

    public static List<Task> createTasks(int num) {
        List<Task> tasks = new ArrayList<>();

        for (int i = 0; i < num; i++) {
            tasks.add(Task.newBuilder()
                    .build());
        }
        return tasks;
    }
}
