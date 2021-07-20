package com.miotech.kun.metadata.web.service;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.core.model.event.MetadataStatisticsEvent;
import com.miotech.kun.metadata.databuilder.constant.OperatorKey;
import com.miotech.kun.metadata.web.constant.PropKey;
import com.miotech.kun.metadata.web.constant.TaskParam;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.facade.WorkflowServiceFacade;

import java.util.Map;

@Singleton
public class MSEService {

    private Props props;
    private WorkflowServiceFacade workflowServiceFacade;

    @Inject
    public MSEService(Props props, WorkflowServiceFacade workflowServiceFacade) {
        this.props = props;
        this.workflowServiceFacade = workflowServiceFacade;
    }

    public TaskRun execute(MetadataStatisticsEvent mse) {
        return workflowServiceFacade.executeTask(props.getLong(TaskParam.MSE_TASK.getName()),
                buildVariablesForTaskRun(mse.getGid(), mse.getSnapshotId(), mse.getEventType()));
    }

    private Map<String, Object> buildVariablesForTaskRun(long gid, long snapshotId, MetadataStatisticsEvent.EventType eventType) {
        Map<String, Object> conf = Maps.newHashMap();
        conf.put(OperatorKey.DATASOURCE_JDBC_URL, props.get(PropKey.JDBC_URL));
        conf.put(OperatorKey.DATASOURCE_USERNAME, props.get(PropKey.USERNAME));
        conf.put(OperatorKey.DATASOURCE_PASSWORD, props.get(PropKey.PASSWORD));
        conf.put(OperatorKey.DATASOURCE_DRIVER_CLASS_NAME, props.get(PropKey.DRIVER_CLASS_NAME));
        conf.put(OperatorKey.GID, String.valueOf(gid));
        conf.put(OperatorKey.SNAPSHOT_ID, String.valueOf(snapshotId));
        conf.put(OperatorKey.STATISTICS_MODE, eventType.name());

        return conf;
    }

}
