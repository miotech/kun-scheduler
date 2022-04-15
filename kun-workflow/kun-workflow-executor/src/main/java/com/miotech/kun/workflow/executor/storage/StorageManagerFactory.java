package com.miotech.kun.workflow.executor.storage;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.StorageManager;

@Singleton
public class StorageManagerFactory {

    private final TaskRunDao taskRunDao;

    @Inject
    public StorageManagerFactory(TaskRunDao taskRunDao) {
        this.taskRunDao = taskRunDao;
    }

    public StorageManager createStorageManager(StorageType type){
        StorageManager storageManager = null;
        switch (type){
            case LOCAL:
                storageManager = new LocalStorageManager(taskRunDao);
        }
        return storageManager;
    }
}
