package com.miotech.kun.workflow.core.model.lineage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.utils.JsonLongFieldDeserializer;

import java.io.Serializable;
import java.util.List;

/**
 * This class is the value object representation of `com.miotech.kun.workflow.common.lineage.node.DatasetNode`,
 * Remember to update this object when DatasetNode schema is updated
 */
@JsonDeserialize(builder = DatasetNodeInfo.DatasetNodeInfoBuilder.class)
public class DatasetNodeInfo implements Serializable {
    @JsonIgnore
    private static final long serialVersionUID = -1603678269678L;

    /**
     * Global id of dataset
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = JsonLongFieldDeserializer.class)
    private final Long gid;

    private final String datasetName;

    /** List of tasks this dataset serves as input (this dataset -[is input of]-> tasks) */
    private final List<Task> downstreamTasks;

    /** List of tasks this dataset serves as output (tasks -[output to]-> this dataset) */
    private final List<Task> upstreamTasks;

    public DatasetNodeInfo(Long gid, String datasetName, List<Task> downstreamTasks, List<Task> upstreamTasks) {
        this.gid = gid;
        this.datasetName = datasetName;
        this.downstreamTasks = downstreamTasks;
        this.upstreamTasks = upstreamTasks;
    }

    public static DatasetNodeInfoBuilder newBuilder() {
        return new DatasetNodeInfoBuilder();
    }

    public Long getGid() {
        return gid;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public List<Task> getDownstreamTasks() {
        return downstreamTasks;
    }

    public List<Task> getUpstreamTasks() {
        return upstreamTasks;
    }

    public DatasetNodeInfoBuilder cloneBuilder() {
        return new DatasetNodeInfoBuilder()
                .withGid(gid)
                .withDatasetName(datasetName)
                .withDownstreamTasks(downstreamTasks)
                .withUpstreamTasks(upstreamTasks);
    }

    @JsonPOJOBuilder
    public static final class DatasetNodeInfoBuilder {
        private Long gid;
        private String datasetName;
        private List<Task> downstreamTasks;
        private List<Task> upstreamTasks;

        private DatasetNodeInfoBuilder() {
        }

        public DatasetNodeInfoBuilder withGid(Long gid) {
            this.gid = gid;
            return this;
        }

        public DatasetNodeInfoBuilder withDatasetName(String datasetName) {
            this.datasetName = datasetName;
            return this;
        }

        public DatasetNodeInfoBuilder withDownstreamTasks(List<Task> downstreamTasks) {
            this.downstreamTasks = downstreamTasks;
            return this;
        }

        public DatasetNodeInfoBuilder withUpstreamTasks(List<Task> upstreamTasks) {
            this.upstreamTasks = upstreamTasks;
            return this;
        }

        public DatasetNodeInfo build() {
            return new DatasetNodeInfo(gid, datasetName, downstreamTasks, upstreamTasks);
        }
    }
}
