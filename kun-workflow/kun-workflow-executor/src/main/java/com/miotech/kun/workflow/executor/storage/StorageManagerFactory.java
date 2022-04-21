package com.miotech.kun.workflow.executor.storage;

import com.miotech.kun.workflow.core.StorageManager;

public class StorageManagerFactory {


    public static StorageManager createStorageManager(StorageType type){
        StorageManager storageManager = null;
        switch (type){
            case LOCAL:
                storageManager = new LocalStorageManager();
        }
        return storageManager;
    }
}
