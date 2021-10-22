package com.miotech.kun.dataplatform.web.common.commit.service;

import com.google.common.base.Preconditions;
import com.miotech.kun.dataplatform.facade.model.commit.CommitStatus;
import com.miotech.kun.dataplatform.facade.model.commit.CommitType;
import com.miotech.kun.dataplatform.facade.model.commit.TaskCommit;
import com.miotech.kun.dataplatform.facade.model.commit.TaskSnapshot;
import com.miotech.kun.dataplatform.facade.model.taskdefinition.ScheduleConfig;
import com.miotech.kun.dataplatform.facade.model.taskdefinition.TaskDefinition;
import com.miotech.kun.dataplatform.web.common.commit.dao.TaskCommitDao;
import com.miotech.kun.dataplatform.web.common.commit.dao.VersionProps;
import com.miotech.kun.dataplatform.web.common.commit.vo.CommitSearchRequest;
import com.miotech.kun.dataplatform.web.common.commit.vo.TaskCommitVO;
import com.miotech.kun.dataplatform.web.common.taskdefinition.service.TaskDefinitionService;
import com.miotech.kun.dataplatform.web.common.utils.DataPlatformIdGenerator;
import com.miotech.kun.dataplatform.web.common.utils.VersionUtil;
import com.miotech.kun.dataplatform.web.exception.CannotGenerateNextTickException;
import com.miotech.kun.dataplatform.web.exception.InvalidCronExpressionException;
import com.miotech.kun.security.service.BaseSecurityService;
import com.miotech.kun.workflow.client.model.PaginationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.miotech.kun.workflow.utils.CronUtils.*;

@Service
@Slf4j
public class TaskCommitService extends BaseSecurityService {

    @Autowired
    private TaskCommitDao taskCommitDao;

    @Autowired
    private TaskDefinitionService taskDefinitionService;

    public TaskCommit find(Long commitId) {
        return taskCommitDao.fetchById(commitId)
                .<IllegalArgumentException>orElseThrow(() -> {
                    throw new IllegalArgumentException(String.format("Task Commit not found: \"%s\"", commitId));
                });
    }

    public List<TaskCommit> find(List<Long> commitIds) {
        Map<Long, TaskCommit> resultMap = taskCommitDao
                .fetchByIds(commitIds).stream()
                .collect(Collectors.toMap(TaskCommit::getId, Function.identity()));
        commitIds.forEach(x -> {
            if (resultMap.get(x) == null) {
                throw new IllegalArgumentException(String.format("Task Commit not found: \"%s\"", x));
            }
        });
        return new ArrayList<>(resultMap.values());
    }

    public List<TaskCommit> findByTaskDefinitionId(Long taskDefinitionId) {
        Preconditions.checkNotNull(taskDefinitionId, "task definition id should not be null");
        return taskCommitDao.fetchByTaskDefinitionId(taskDefinitionId);
    }

    public PaginationResult<TaskCommit> search(CommitSearchRequest request) {
        Preconditions.checkArgument(request.getPageNum() > 0, "page number should be a positive number");
        Preconditions.checkArgument(request.getPageSize() > 0, "page size should be a positive number");
        return taskCommitDao.search(request);
    }

    /**
     * Commit a task definition, support CREATED, MODIFIED, OFFLINE
     *
     * @param taskDefinitionId
     * @param commitMessage
     * @return
     */
    @Transactional
    public TaskCommit commit(Long taskDefinitionId, String commitMessage) {
        TaskDefinition taskDefinition = taskDefinitionService.find(taskDefinitionId);

        Optional<VersionProps> versionProps = taskCommitDao.fetchLatestVersionByTask(taskDefinitionId);
        // initial state
        int version = 1;
        CommitType commitType = CommitType.CREATED;

        if (versionProps.isPresent()) {
            version = versionProps.get().getVersion() + 1;
            commitType = taskDefinition.isArchived() ? CommitType.OFFLINE : CommitType.MODIFIED;
        } else if (taskDefinition.isArchived()) {
            log.debug("Task definition {} has not commit yet and delete now, should not commit", taskDefinitionId);
            return null;
        }
        // if not offline, check taskdefinition and prepare for commit
        if (CommitType.OFFLINE != commitType) {
            taskDefinitionService.checkRule(taskDefinition);
        }
        TaskSnapshot snapshot = TaskSnapshot.newBuilder()
                .withName(taskDefinition.getName())
                .withTaskTemplateName(taskDefinition.getTaskTemplateName())
                .withTaskPayload(taskDefinition.getTaskPayload())
                .withOwner(taskDefinition.getOwner())
                .build();
        validateSnapshotCronExpression(snapshot);
        TaskCommit commit = TaskCommit.newBuilder()
                .withId(DataPlatformIdGenerator.nextCommitId())
                .withDefinitionId(taskDefinitionId)
                .withMessage(commitMessage)
                .withSnapshot(snapshot)
                .withVersion(VersionUtil.getVersion(version))
                .withCommittedAt(OffsetDateTime.now())
                .withCommitType(commitType)
                .withCommitStatus(CommitStatus.SUBMITTED)
                .withCommitter(getCurrentUser().getId())
                .withLatestCommit(true)
                .build();
        // update previous commit and create new commit
        versionProps.ifPresent(props -> taskCommitDao.updateCommitLatestFlag(props.getId(), false));
        return taskCommitDao.create(commit);
    }

    /**
     * Validate Cron expression in snapshot. Throws exception if cron expression cannot generate next tick.
     */
    private void validateSnapshotCronExpression(TaskSnapshot snapshot) {
        Preconditions.checkNotNull(snapshot.getTaskPayload(), "Task payload of generated snapshot cannot be null");
        Preconditions.checkNotNull(snapshot.getTaskPayload().getScheduleConfig(), "Schedule config of generated snapshot cannot be null");
        ScheduleConfig scheduleConfig = snapshot.getTaskPayload().getScheduleConfig();

        String type = scheduleConfig.getType();
        // The validation shall pass when
        if (!Objects.equals(type, "NONE")) {
            String cronExpression = scheduleConfig.getCronExpr();
            Preconditions.checkNotNull(cronExpression, "Cron expression cannot be null when schedule type is not \"manual\".");
            try {
                validateCron(convertStringToCron(cronExpression));
            } catch (IllegalArgumentException e) {
                throw new InvalidCronExpressionException(cronExpression);
            }
            Optional<OffsetDateTime> nextTickTime = getNextExecutionTimeFromNow(convertStringToCron(cronExpression));
            // If next tick cannot be computed
            if (!nextTickTime.isPresent()) {
                throw new CannotGenerateNextTickException(cronExpression);
            }
        }
    }

    public TaskCommitVO convertVO(TaskCommit commit) {
        return new TaskCommitVO(
                commit.getId(),
                commit.getDefinitionId(),
                commit.getVersion(),
                commit.getMessage(),
                commit.getSnapshot(),
                commit.getCommitter(),
                commit.getCommittedAt(),
                commit.getCommitType(),
                commit.getCommitStatus(),
                commit.isLatestCommit()
        );
    }

    public Map<Long, Boolean> getLatestCommitStatus(List<Long> definitionIds) {
        return taskCommitDao.getLatestCommitStatus(definitionIds);
    }
}