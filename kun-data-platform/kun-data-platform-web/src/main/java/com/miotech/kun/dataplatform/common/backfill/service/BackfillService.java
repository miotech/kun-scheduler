package com.miotech.kun.dataplatform.common.backfill.service;

import com.google.common.base.Preconditions;
import com.miotech.kun.common.model.PageResult;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataplatform.common.backfill.dao.BackfillDao;
import com.miotech.kun.dataplatform.common.backfill.vo.BackfillCreateInfo;
import com.miotech.kun.dataplatform.common.backfill.vo.BackfillSearchParams;
import com.miotech.kun.dataplatform.model.backfill.Backfill;
import com.miotech.kun.security.model.UserInfo;
import com.miotech.kun.security.service.BaseSecurityService;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class BackfillService extends BaseSecurityService {
    @Autowired
    private BackfillDao backfillDao;

    @Autowired
    private WorkflowClient workflowClient;

    /** 按照 id 查询 Backfill instance */
    public Optional<Backfill> fetchById(Long backfillId) {
        Preconditions.checkNotNull(backfillId, "Argument `id` should not be null.");
        return backfillDao.fetchById(backfillId);
    }

    /** 分页搜索 backfill */
    public PageResult<Backfill> search(BackfillSearchParams searchParams) {
        Preconditions.checkNotNull(searchParams, "search parameters cannot be null");
        if (searchParams.getPageSize() != null && searchParams.getPageSize() > 100) {
            log.debug("Large page size detected. Adjusting page size to 100");
            searchParams.setPageSize(100);
        }
        return backfillDao.search(searchParams);
    }

    /** 创建一个 backfill */
    @Transactional
    public Backfill create(BackfillCreateInfo createInfo) {
        UserInfo userInfo = getCurrentUser();
        if (Objects.isNull(userInfo)) {
            throw new IllegalStateException("Cannot get information of current user.");
        }
        OffsetDateTime now = DateTimeUtils.now();
        Backfill backfillToCreate = Backfill.newBuilder()
                .withId(IdGenerator.getInstance().nextId())
                .withName(createInfo.getName())
                .withCreator(getCurrentUser().getId())
                .withTaskRunIds(createInfo.getTaskRunIds())
                .withTaskDefinitionIds(createInfo.getTaskDefinitionIds())
                .withCreateTime(now)
                .withUpdateTime(now)
                .build();
        return backfillDao.create(backfillToCreate);
    }

    /** 按 id 执行对应的 backfill。若成功返回 true，若 Backfill 不存在返回 false */
    @Transactional
    public boolean runBackfillById(Long backfillId) {
        // preconditions check
        Preconditions.checkNotNull(backfillId, "Backfill id cannot be null");

        Optional<Backfill> backfillOptional = backfillDao.fetchById(backfillId);
        if (!backfillOptional.isPresent()) {
            log.debug("Cannot find target backfill with id: {}", backfillId);
            return false;
        }

        // else
        // TODO: invoke workflow client APIs
        return true;
    }

    /** 创建一个 backfill 并立即执行 */
    @Transactional
    public Backfill createAndRun(BackfillCreateInfo createInfo) {
        Backfill backfill = create(createInfo);
        this.runBackfillById(backfill.getId());
        return backfill;
    }

    /** 按照 id 查询 Backfill instance 实例对应的 Task run instances 列表 */
    public List<TaskRun> fetchTaskRunsByBackfillId(long backfillId) {
        // TODO: complete this
        return null;
    }

    /** 停止 id 对应的 Backfill 中的所有 TaskRuns。若停止成功，返回 true；若停止失败（例如：当前 backfill 已经完成/停止，或者 backfill 不存在）则返回 false */
    public boolean stopBackfillById(long backfillId) {
        // TODO: complete this
        return false;
    }
}
