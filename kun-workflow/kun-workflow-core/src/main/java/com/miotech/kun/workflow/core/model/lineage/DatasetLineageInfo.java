package com.miotech.kun.workflow.core.model.lineage;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.List;

public class DatasetLineageInfo implements Serializable {
    @JsonIgnore
    private static final long serialVersionUID = -1603513011096L;

    private DatasetLineageInfo(DatasetNodeInfo sourceNode, List<DatasetNodeInfo> downstreamNodes, List<DatasetNodeInfo> upstreamNodes, Integer queryDepth) {
        this.sourceNode = sourceNode;
        this.downstreamNodes = downstreamNodes;
        this.upstreamNodes = upstreamNodes;
        this.queryDepth = queryDepth;
    }

    private final DatasetNodeInfo sourceNode;

    private final List<DatasetNodeInfo> downstreamNodes;

    private final List<DatasetNodeInfo> upstreamNodes;

    private final Integer queryDepth;

    public static DatasetLineageInfoBuilder newBuilder() {
        return new DatasetLineageInfoBuilder();
    }

    public DatasetNodeInfo getSourceNode() {
        return sourceNode;
    }

    public List<DatasetNodeInfo> getDownstreamNodes() {
        return downstreamNodes;
    }

    public List<DatasetNodeInfo> getUpstreamNodes() {
        return upstreamNodes;
    }

    public Integer getQueryDepth() {
        return queryDepth;
    }

    public DatasetLineageInfoBuilder cloneBuilder() {
        return new DatasetLineageInfoBuilder()
                .withSourceNode(sourceNode)
                .withUpstreamNodes(upstreamNodes)
                .withDownstreamNodes(downstreamNodes)
                .withQueryDepth(queryDepth);
    }

    public static final class DatasetLineageInfoBuilder {
        private DatasetNodeInfo sourceNode;
        private List<DatasetNodeInfo> downstreamNodes;
        private List<DatasetNodeInfo> upstreamNodes;
        private Integer queryDepth;

        private DatasetLineageInfoBuilder() {
        }

        public DatasetLineageInfoBuilder withSourceNode(DatasetNodeInfo sourceNode) {
            this.sourceNode = sourceNode;
            return this;
        }

        public DatasetLineageInfoBuilder withDownstreamNodes(List<DatasetNodeInfo> downstreamNodes) {
            this.downstreamNodes = downstreamNodes;
            return this;
        }

        public DatasetLineageInfoBuilder withUpstreamNodes(List<DatasetNodeInfo> upstreamNodes) {
            this.upstreamNodes = upstreamNodes;
            return this;
        }

        public DatasetLineageInfoBuilder withQueryDepth(Integer queryDepth) {
            this.queryDepth = queryDepth;
            return this;
        }

        public DatasetLineageInfo build() {
            return new DatasetLineageInfo(sourceNode, downstreamNodes, upstreamNodes, queryDepth);
        }
    }
}
