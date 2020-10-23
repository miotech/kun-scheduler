package com.miotech.kun.workflow.core.model.lineage;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.workflow.utils.JsonLongFieldDeserializer;

import java.util.List;

@JsonDeserialize(builder = EdgeInfo.EdgeInfoBuilder.class)
public class EdgeInfo {
    private final List<EdgeTaskInfo> taskInfos;

    @JsonSerialize(using= ToStringSerializer.class)
    @JsonDeserialize(using = JsonLongFieldDeserializer.class)
    private final Long upstreamDatasetGid;

    @JsonSerialize(using= ToStringSerializer.class)
    @JsonDeserialize(using = JsonLongFieldDeserializer.class)
    private final Long downstreamDatasetGid;

    private EdgeInfo(List<EdgeTaskInfo> taskInfos, Long upstreamDatasetGid, Long downstreamDatasetGid) {
        this.taskInfos = taskInfos;
        this.upstreamDatasetGid = upstreamDatasetGid;
        this.downstreamDatasetGid = downstreamDatasetGid;
    }

    public List<EdgeTaskInfo> getTaskInfos() {
        return taskInfos;
    }

    public Long getUpstreamDatasetGid() {
        return upstreamDatasetGid;
    }

    public Long getDownstreamDatasetGid() {
        return downstreamDatasetGid;
    }

    public static EdgeInfoBuilder newBuilder() {
        return new EdgeInfoBuilder();
    }

    public EdgeInfoBuilder cloneBuilder() {
        EdgeInfoBuilder builder = new EdgeInfoBuilder();
        builder.taskInfos = taskInfos;
        return builder;
    }

    @JsonPOJOBuilder
    public static class EdgeInfoBuilder {
        private List<EdgeTaskInfo> taskInfos;
        private Long upstreamDatasetGid;
        private Long downstreamDatasetGid;

        private EdgeInfoBuilder() {
        }

        public EdgeInfoBuilder withTaskInfos(List<EdgeTaskInfo> taskInfos) {
            this.taskInfos = taskInfos;
            return this;
        }

        public EdgeInfoBuilder withUpstreamDatasetGid(Long upstreamDatasetGid) {
            this.upstreamDatasetGid = upstreamDatasetGid;
            return this;
        }

        public EdgeInfoBuilder withDownstreamDatasetGid(Long downstreamDatasetGid) {
            this.downstreamDatasetGid = downstreamDatasetGid;
            return this;
        }

        public EdgeInfo build() {
            return new EdgeInfo(taskInfos, upstreamDatasetGid, downstreamDatasetGid);
        }
    }
}
