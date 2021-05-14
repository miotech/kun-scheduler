package com.miotech.kun.metadata.core.model.process;

import java.time.OffsetDateTime;

/**
 * Data model that represents a metadata process which pulls schema information on a datasource
 */
public class PullDataSourceProcess extends PullProcess {
    /**
     * Id of the corresponding workflow task run id.
     */
    private final Long mceTaskRunId;

    /**
     * Id of target datasource to pull.
     */
    private final String dataSourceId;

    private PullDataSourceProcess(PullDataSourceProcessBuilder builder) {
        this.processId = builder.processId;
        this.mceTaskRunId = builder.mceTaskRunId;
        this.createdAt = builder.createdAt;
        this.dataSourceId = builder.dataSourceId;
    }

    @Override
    public PullProcessType getProcessType() {
        return PullProcessType.DATASOURCE;
    }

    public Long getMceTaskRunId() {
        return mceTaskRunId;
    }

    public String getDataSourceId() {
        return dataSourceId;
    }

    public static PullDataSourceProcessBuilder newBuilder() {
        return new PullDataSourceProcessBuilder();
    }

    public PullDataSourceProcessBuilder cloneBuilder() {
        PullDataSourceProcessBuilder builder = newBuilder();
        builder.mceTaskRunId = this.mceTaskRunId;
        builder.dataSourceId = this.dataSourceId;
        builder.createdAt = this.createdAt;
        return builder;
    }

    public static final class PullDataSourceProcessBuilder {
        private Long processId;
        private Long mceTaskRunId;
        private String dataSourceId;
        private OffsetDateTime createdAt;

        private PullDataSourceProcessBuilder() {
        }

        public static PullDataSourceProcessBuilder aPullHistoryRecord() {
            return new PullDataSourceProcessBuilder();
        }

        public PullDataSourceProcessBuilder withProcessId(Long processId) {
            this.processId = processId;
            return this;
        }

        public PullDataSourceProcessBuilder withMceTaskRunId(Long mceTaskRunId) {
            this.mceTaskRunId = mceTaskRunId;
            return this;
        }

        public PullDataSourceProcessBuilder withDataSourceId(String dataSourceId) {
            this.dataSourceId = dataSourceId;
            return this;
        }

        public PullDataSourceProcessBuilder withCreatedAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public PullDataSourceProcess build() {
            return new PullDataSourceProcess(this);
        }
    }
}
