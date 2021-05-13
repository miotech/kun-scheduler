package com.miotech.kun.metadata.web.model.vo;

import com.miotech.kun.metadata.core.model.process.PullDataSourceProcess;

import java.time.OffsetDateTime;

public class PullDataSourceProcessVO {
    private String datasourceId;

    private String mceTaskRunId;

    private OffsetDateTime createdAt;

    public static PullDataSourceProcessVO from(PullDataSourceProcess pullDataSourceProcess) {
        PullDataSourceProcessVO vo = new PullDataSourceProcessVO();
        vo.setMceTaskRunId(pullDataSourceProcess.getMceTaskRunId().toString());
        vo.setDatasourceId(pullDataSourceProcess.getDataSourceId());
        vo.setCreatedAt(pullDataSourceProcess.getCreatedAt());
        return vo;
    }

    public String getDatasourceId() {
        return datasourceId;
    }

    public String getMceTaskRunId() {
        return mceTaskRunId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setDatasourceId(String datasourceId) {
        this.datasourceId = datasourceId;
    }

    public void setMceTaskRunId(String mceTaskRunId) {
        this.mceTaskRunId = mceTaskRunId;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
