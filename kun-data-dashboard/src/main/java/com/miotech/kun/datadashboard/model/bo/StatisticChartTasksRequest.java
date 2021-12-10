package com.miotech.kun.datadashboard.model.bo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.miotech.kun.common.model.PageInfo;
import com.miotech.kun.commons.utils.CustomDateTimeDeserializer;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.OffsetDateTime;

@EqualsAndHashCode(callSuper = false)
@Data
public class StatisticChartTasksRequest extends PageInfo {

    Integer timezoneOffset;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    OffsetDateTime targetTime;

    String status;

    TaskRunStatus finalStatus;
}
