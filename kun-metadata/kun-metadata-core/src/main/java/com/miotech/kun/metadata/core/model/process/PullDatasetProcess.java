package com.miotech.kun.metadata.core.model.process;

import java.time.OffsetDateTime;

/**
 *
 */
public class PullDatasetProcess extends PullProcess {
    private static final PullProcessType processType = PullProcessType.DATASET;

    /**
     * Dataset id
     */
    private final String datasetId;

    /**
     * Id of the corresponding MCE workflow task run id.
     */
    private final Long mceTaskRunId;

    /**
     * Id of the corresponding MSE workflow task run id.
     */
    private final Long mseTaskRunId;

    public String getDatasetId() {
        return datasetId;
    }

    public Long getMceTaskRunId() {
        return mceTaskRunId;
    }

    public Long getMseTaskRunId() {
        return mseTaskRunId;
    }

    public static PullDatasetProcessBuilder newBuilder() {
        return new PullDatasetProcessBuilder();
    }

    private PullDatasetProcess(PullDatasetProcessBuilder builder) {
        this.processId = builder.processId;
        this.datasetId = builder.datasetId;
        this.mceTaskRunId = builder.mceTaskRunId;
        this.mseTaskRunId = builder.mseTaskRunId;
        this.createdAt = builder.createdAt;
    }

    public static final class PullDatasetProcessBuilder {
        protected Long processId;
        protected OffsetDateTime createdAt;
        private String datasetId;
        private Long mceTaskRunId;
        private Long mseTaskRunId;

        private PullDatasetProcessBuilder() {
        }


        public PullDatasetProcessBuilder withDatasetId(String datasetId) {
            this.datasetId = datasetId;
            return this;
        }

        public PullDatasetProcessBuilder withMceTaskRunId(Long mceTaskRunId) {
            this.mceTaskRunId = mceTaskRunId;
            return this;
        }

        public PullDatasetProcessBuilder withMseTaskRunId(Long mseTaskRunId) {
            this.mseTaskRunId = mseTaskRunId;
            return this;
        }

        public PullDatasetProcessBuilder withProcessId(Long processId) {
            this.processId = processId;
            return this;
        }

        public PullDatasetProcessBuilder withCreatedAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public PullDatasetProcess build() {
            return new PullDatasetProcess(this);
        }
    }
}
