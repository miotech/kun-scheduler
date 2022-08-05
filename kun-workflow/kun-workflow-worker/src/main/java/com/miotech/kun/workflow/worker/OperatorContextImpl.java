package com.miotech.kun.workflow.worker;

import com.google.inject.Inject;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.execution.OperatorContext;
import com.miotech.kun.workflow.core.model.executetarget.ExecuteTarget;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.resource.Resource;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

public class OperatorContextImpl implements OperatorContext {
    private final Config config;
    private final Long taskRunId;
    private final ExecuteTarget executeTarget;
    private final String queueName;

    @Inject
    private TaskRunDao taskRunDao;

    static final Logger logger = LoggerFactory.getLogger(OperatorContextImpl.class);

    public OperatorContextImpl(Config config, Long taskRunId, ExecuteTarget executeTarget, String queueName) {
        this.config = config;
        this.taskRunId = taskRunId;
        this.executeTarget = executeTarget;
        this.queueName = queueName;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public Long getTaskRunId() {
        return taskRunId;
    }

    @Override
    public Resource getResource(String path) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public String getScheduleTime(){
        String DEFAULT_TICK = "000000000000";
        String scheduleTime = taskRunDao.getScheduleTimeByTaskRunId(taskRunId);
        if (!Strings.isNullOrEmpty(scheduleTime)) {
            return getTime(scheduleTime);
        }
        String tick = taskRunDao.getTickByTaskRunId(taskRunId);
        if(!Strings.isNullOrEmpty(tick)){
            return getTime(tick);
        }
        return DEFAULT_TICK;
    }


    private String getTime(String tick) {
        String PATTERN = "yyyyMMddHHmm";
        Optional<TaskRun> taskRun = taskRunDao.fetchTaskRunById(taskRunId);
        if(!taskRun.isPresent()){
            throw new IllegalArgumentException("task run not found: " + taskRunId.toString());
        }
        String userDefinedTimeZone = taskRun.get().getTask().getScheduleConf().getTimeZone();
        if(Strings.isNullOrEmpty(userDefinedTimeZone)){
            userDefinedTimeZone = "UTC";
        }

        try {
            SimpleDateFormat formatter = new SimpleDateFormat(PATTERN);
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date tickTimeUTC = formatter.parse(tick);
            formatter.setTimeZone(TimeZone.getTimeZone(userDefinedTimeZone));
            String tickTimeWithUserDefinedTimezone = formatter.format(tickTimeUTC);
            logger.info(String.format("Tick %s being parsed to %s with timezone %s", tick, tickTimeWithUserDefinedTimezone, userDefinedTimeZone));
            return tickTimeWithUserDefinedTimezone;
        } catch (ParseException e) {
            logger.error("parse timestamp failed", e);
            throw new IllegalArgumentException("timestamp format " + PATTERN + " can not be parsed");
        }
    }

    @Override
    public ExecuteTarget getExecuteTarget() {
        return executeTarget;
    }

    @Override
    public String getQueueName() {
        return queueName;
    }
}

