package com.miotech.kun.monitor.facade.alert;

public interface NotifyFacade {

    void notify(Long workflowTaskId, String msg);

}
