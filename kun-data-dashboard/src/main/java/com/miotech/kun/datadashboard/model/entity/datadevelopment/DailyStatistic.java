package com.miotech.kun.datadashboard.model.entity.datadevelopment;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.common.utils.DateUtils;
import com.miotech.kun.commons.utils.CustomDateTimeDeserializer;
import com.miotech.kun.commons.utils.NumberUtils;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class DailyStatistic {

    OffsetDateTime time;

    Integer totalCount;

    List<TaskResult> taskResultList;


    public DailyStatistic(OffsetDateTime time, Integer totalCount, List<TaskResult> taskResultList) {
        this.time = time;
        this.totalCount = totalCount;
        this.taskResultList = taskResultList;
    }
}
