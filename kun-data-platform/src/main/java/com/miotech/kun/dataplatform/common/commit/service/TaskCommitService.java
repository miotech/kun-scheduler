package com.miotech.kun.dataplatform.common.commit.service;

import com.google.common.base.Preconditions;
import com.miotech.kun.dataplatform.common.commit.dao.TaskCommitDao;
import com.miotech.kun.dataplatform.common.commit.dao.VersionProps;
import com.miotech.kun.dataplatform.common.commit.vo.CommitSearchRequest;
import com.miotech.kun.dataplatform.common.commit.vo.TaskCommitVO;
import com.miotech.kun.dataplatform.common.taskdefinition.service.TaskDefinitionService;
import com.miotech.kun.dataplatform.common.utils.DataPlatformIdGenerator;
import com.miotech.kun.dataplatform.common.utils.VersionUtil;
import com.miotech.kun.dataplatform.model.commit.CommitStatus;
import com.miotech.kun.dataplatform.model.commit.CommitType;
import com.miotech.kun.dataplatform.model.commit.TaskCommit;
import com.miotech.kun.dataplatform.model.commit.TaskSnapshot;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskDefinition;
import com.miotech.kun.security.service.BaseSecurityService;
import com.miotech.kun.workflow.client.model.PaginationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public TaskCommitVO convertVO(TaskCommit commit) {
        return new TaskCommitVO(
                commit.getId(),
                commit.getDefinitionId(),
                commit.getVersion(),
                commit.getMessage(),
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