package com.miotech.kun.workflow.core.model.lineage;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.workflow.utils.JsonLongFieldDeserializer;

import java.util.List;

/**
 * This class is the value object representation of `com.miotech.kun.workflow.common.lineage.node.DatasetNode`,
 * Remember to update this object when DatasetNode schema is updated
 */
@JsonDeserialize(builder = DatasetNodeInfo.DatasetNodeInfoBuilder.class)
public class DatasetNodeInfo {
    /**
     * Global id of dataset
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = JsonLongFieldDeserializer.class)
    private final Long gid;

    private final String datasetName;

    /** List of task ids this dataset serves as input (this dataset -[is input of]-> tasks) */
    private final List<Long> downstreamTaskIds;

    /** List of task ids this dataset serves as output (tasks -[output to]-> this dataset) */
    private final List<Long> upstreamTaskIds;

    public DatasetNodeInfo(Long gid, String datasetName, List<Long> downstreamTaskIds, List<Long> upstreamTaskIds) {
        this.gid = gid;
        this.datasetName = datasetName;
        this.downstreamTaskIds = downstreamTaskIds;
        this.upstreamTaskIds = upstreamTaskIds;
    }

    public Long getGid() {
        return gid;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public List<Long> getDownstreamTaskIds() {
        return downstreamTaskIds;
    }

    public List<Long> getUpstreamTaskIds() {
        return upstreamTaskIds;
    }

    public static DatasetNodeInfoBuilder newBuilder() {
        return new DatasetNodeInfoBuilder();
    }

    public DatasetNodeInfoBuilder cloneBuilder() {
        return new DatasetNodeInfoBuilder()
                .withGid(gid)
                .withDatasetName(datasetName)
                .withDownstreamTaskIds(downstreamTaskIds)
                .withUpstreamTaskIds(upstreamTaskIds);
    }

    @JsonPOJOBuilder
    public static final class DatasetNodeInfoBuilder {
        private Long gid;
        private String datasetName;
        private List<Long> downstreamTaskIds;
        private List<Long> upstreamTaskIds;

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

        public DatasetNodeInfoBuilder withDownstreamTaskIds(List<Long> downstreamTaskIds) {
            this.downstreamTaskIds = downstreamTaskIds;
            return this;
        }

        public DatasetNodeInfoBuilder withUpstreamTaskIds(List<Long> upstreamTaskIds) {
            this.upstreamTaskIds = upstreamTaskIds;
            return this;
        }

        public DatasetNodeInfo build() {
            return new DatasetNodeInfo(gid, datasetName, downstreamTaskIds, upstreamTaskIds);
        }
    }
}
