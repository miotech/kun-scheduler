package com.miotech.kun.monitor.facade.alert;

import java.util.List;

public interface NotifyFacade {

    void notify(Long workflowTaskId, String msg);

    void notify(List<String> usernames, String msg);

}
