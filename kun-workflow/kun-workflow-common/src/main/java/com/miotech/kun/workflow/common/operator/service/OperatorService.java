package com.miotech.kun.workflow.common.operator.service;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.common.exception.EntityNotFoundException;
import com.miotech.kun.workflow.common.exception.RuleOperatorInUseException;
import com.miotech.kun.workflow.common.operator.dao.OperatorDao;
import com.miotech.kun.workflow.common.operator.filter.OperatorSearchFilter;
import com.miotech.kun.workflow.common.operator.vo.OperatorPropsVO;
import com.miotech.kun.workflow.common.resource.ResourceService;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.task.vo.OperatorVO;
import com.miotech.kun.workflow.common.task.vo.PaginationVO;
import com.miotech.kun.workflow.core.execution.ConfigDef;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.resource.Resource;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.miotech.kun.workflow.common.constant.ConfigurationKeys.PROP_RESOURCE_LIBDIRECTORY;

@Singleton
public class OperatorService {
    private static final Logger logger = LoggerFactory.getLogger(OperatorService.class);

    @Inject
    private OperatorDao operatorDao;

    @Inject
    private TaskDao taskDao;

    @Inject
    private ResourceService resourceService;

    @Inject
    private Props props;

    private static final int PAGE_NUM_DEFAULT = 1;

    private static final int PAGE_SIZE_DEFAULT = Integer.MAX_VALUE;

    private final LoadingCache<Long, Class<? extends KunOperator>> operatorCache = CacheBuilder.newBuilder()
            .maximumSize(1024)
            .build(new CacheLoader<Long, Class<? extends KunOperator>>() {
                @Override
                public Class<? extends KunOperator> load(Long operatorId) throws Exception {
                    return loadOperatorClass(operatorId);
                }
            });

    /**
     * 加载一个Operator
     *
     * @param operatorId
     * @return
     */
    public KunOperator loadOperator(Long operatorId) {
        return loadOperator(operatorId, false);
    }

    /**
     * 加载一个Operator
     *
     * @param operatorId
     * @return
     */
    public KunOperator loadOperator(Long operatorId, boolean force) {
        checkNotNull(operatorId, "operatorId should not be null.");
        try {
            if (force) {
                refreshOperatorCache(operatorId);
            }
            return operatorCache.get(operatorId).newInstance();
        } catch (Exception e) {
            logger.error("Failed to load operator. operatorId={}", operatorId, e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    /**
     * 加载一个Operator（不使用缓存）
     *
     * @param operatorId
     * @return
     */
    public Class<? extends KunOperator> loadOperatorClass(Long operatorId) {
        checkNotNull(operatorId, "operatorId should not be null.");
        Operator operator = findOperator(operatorId);
        return loadOperatorClass0(operator.getPackagePath(), operator.getClassName());
    }

    /**
     * 获取Operator的配置项定义
     *
     * @param operatorId
     * @return
     */
    public ConfigDef getOperatorConfigDef(Long operatorId) {
        ConfigDef configDef = loadOperator(operatorId).config();
        checkNotNull(configDef, "configDef of operator={} should not be null.", operatorId);
        return configDef;
    }

    public Operator findOperator(Long operatorId) {
        return operatorDao.fetchById(operatorId)
                .orElseThrow(() -> new IllegalArgumentException("Operator is not found for id: " + operatorId));
    }

    public Operator createOperator(OperatorPropsVO vo) {
        // 1. Check integrity of operator properties VO
        validatePropsVOIntegrity(vo);

        // 2. Operator name should not be duplicated
        if (operatorDao.fetchByName(vo.getName()).isPresent()) {
            throw new IllegalArgumentException(String.format("Cannot create operator with duplicated name: \"%s\"", vo.getName()));
        }

        // 3. Assign operator id and generate Operator instance
        Long id = WorkflowIdGenerator.nextOperatorId();
        Operator operator = Operator.newBuilder()
                .withName(vo.getName())
                .withDescription(vo.getDescription())
                .withClassName(vo.getClassName())
                .withId(id)
                .withPackagePath("")
                .build();

        // 4. Persist it and return
        operatorDao.create(operator);
        Optional<Operator> operatorOptional = operatorDao.fetchById(id);
        return operatorOptional.orElse(null);
    }

    public Resource uploadOperatorJar(Long operatorId, List<FileItem> fileItems) {
        Preconditions.checkNotNull(operatorId, "Operator Id should not be null");
        List<FileItem> uploadFiles = fileItems
                .stream()
                .filter(x -> !x.isFormField())
                .collect(Collectors.toList());
        Preconditions.checkArgument(uploadFiles.size() == 1, "Currently only one file supported");
        Operator operator = findOperator(operatorId);
        try {
            FileItem uploadFie =  uploadFiles.get(0);
            String packagePath = generateOperatorPath(operator);
            Resource resource = resourceService.createResource(packagePath, uploadFie.getInputStream());
            operatorDao.updateById(operatorId, operator
                    .cloneBuilder()
                    .withPackagePath(packagePath).build());

            // refresh operator cache
            refreshOperatorCache(operatorId);
            return resource;
        } catch (IOException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    /**
     * Persist an newly created operator object, or update an existing operator.
     * If `id` of `operator` object is not initialized, a generated id will be automatically assigned.
     *
     * @param operator
     * @return
     */
    public Operator saveOperator(Operator operator) {
        // 1. Validate argument
        validateOperatorNotNull(operator);

        // 2. Check if operator is already stored
        boolean operatorExistsFlag;
        if (Objects.nonNull(operator.getId())) {
            operatorExistsFlag = operatorDao.fetchById(operator.getId()).isPresent();
        } else {
            operatorExistsFlag = false;
        }

        if (operatorExistsFlag) {
            // 3. if existed, update operator
            return fullUpdateOperator(
                    operator.getId(),
                    convertOperatorToPropsVO(operator)
            );
        } else {
            // 4. if not existed, create operator
            Long id;
            if (Objects.nonNull(operator.getId())) {
                id = operator.getId();
                operatorDao.create(operator);
            } else {
                id = WorkflowIdGenerator.nextOperatorId();
                operatorDao.createWithId(operator, id);
            }
            return operatorDao.fetchById(id).orElse(null);
        }
    }

    public Optional<Operator> fetchOperatorByName(String operatorName){
        return  operatorDao.fetchByName(operatorName);
    }

    public Operator fullUpdateOperator(Long operatorId, OperatorPropsVO vo) {
        // 1. Check integrity of operator properties VO and id
        validateIdNotNull(operatorId);
        validatePropsVOIntegrity(vo);

        // 2. Updating operator should exists
        Optional<Operator> operatorToBeUpdatedOptional = operatorDao.fetchById(operatorId);
        if (!operatorToBeUpdatedOptional.isPresent()) {
            throw new EntityNotFoundException(String.format("Cannot perform update on non-exist operator with id: %d", operatorId));
        }

        // 3. Property `name` of updated operator should not conflict with another existed operator (except itself)
        Operator operatorToBeUpdated = operatorToBeUpdatedOptional.get();
        boolean nameIsModified = !Objects.equals(vo.getName(), operatorToBeUpdated.getName());
        if (nameIsModified) {
            Optional<Operator> operatorWithConflictNameOptional = operatorDao.fetchByName(vo.getName());
            if (operatorWithConflictNameOptional.isPresent()) {
                throw new IllegalArgumentException(String.format("Cannot update operator with conflict name: \"%s\"", vo.getName()));
            }
        }

        // 4. Update existed
        Operator operator = operatorToBeUpdated
                .cloneBuilder()
                .withName(vo.getName())
                .withDescription(vo.getDescription())
                .withClassName(vo.getClassName())
                .build();
        operatorDao.updateById(operatorId, operator);

        // 5. refresh cache
        refreshOperatorCache(operatorId);

        return operator;
    }

    public Operator partialUpdateOperator(Long operatorId, OperatorPropsVO vo) {
        // 1. Validate arguments
        validateIdNotNull(operatorId);
        checkNotNull(vo, "Cannot perform update with vo: null");

        // 2. If operator not exists, throw exception
        Optional<Operator> fetchedOperator = operatorDao.fetchById(operatorId);
        if (!fetchedOperator.isPresent()) {
            throw new EntityNotFoundException(String.format("Cannot perform update on non-exist operator with id: %d", operatorId));
        }

        // 3. produce a "partially" updated operator from existing one as template
        Operator operator = fetchedOperator.get();
        Operator updatedOperator = operator.cloneBuilder()
                .withId(operatorId)
                .withName(StringUtils.isEmpty(vo.getName()) ? operator.getName() : vo.getName())
                .withDescription(StringUtils.isEmpty(vo.getDescription()) ? operator.getDescription() : vo.getDescription())
                .withClassName(StringUtils.isEmpty(vo.getClassName()) ? operator.getClassName() : vo.getClassName())
                .build();

        // 4. update through DAO
        operatorDao.updateById(operatorId, updatedOperator);

        // 5. refresh cache
        refreshOperatorCache(operatorId);

        return updatedOperator;
    }

    /**
     * Delete an operator.
     * Note that if there is any usage from task, a RuleOperatorInUseException will be thrown.
     *
     * @param operator operator to be deleted
     */
    public void deleteOperator(Operator operator) {
        // 1. validate argument
        validateOperatorNotNull(operator);

        // 2. handle by method `deleteOperatorById`
        deleteOperatorById(operator.getId());
    }

    /**
     * Delete an operator by id.
     * Note that if there is any usage from task, a RuleOperatorInUseException will be thrown.
     * If `id` not exists, a EntityNotFoundException will be thrown.
     *
     * @param id id of the operator to be deleted
     */
    public void deleteOperatorById(Long id) {
        // 1. validate argument
        validateIdNotNull(id);

        // 2. operator should exists. if not, throw exception
        if (!operatorDao.fetchById(id).isPresent()) {
            throw new EntityNotFoundException(String.format("Cannot find operator with id: %d", id));
        }

        // 3. If there is any remaining usage reference from tasks, throw exception
        List<Task> relatedTasks = taskDao.fetchByOperatorId(id);
        if (CollectionUtils.isNotEmpty(relatedTasks)) {
            throw new RuleOperatorInUseException(String.format(
                    "Operator with id \"%d\" is used by %d tasks already. Remove these tasks first or replace internal operators by another operator.",
                    id,
                    relatedTasks.size()
            ));
        }

        // 4. delete operator by DAO
        operatorDao.deleteById(id);

        // 5. refresh cache
        refreshOperatorCache(id);
    }

    public PaginationVO<OperatorVO> fetchOperatorsWithFilter(OperatorSearchFilter filters) {
        // 1. Filter should not be null
        checkNotNull(filters, "Cannot search operators with filter: null");

        // 2. Assign default value for pagination if filter not specified
        Integer pageNum;
        Integer pageSize;
        if (Objects.isNull(filters.getPageNum())) {
            pageNum = PAGE_NUM_DEFAULT;
        } else {
            pageNum = filters.getPageNum();
        }

        if (Objects.isNull(filters.getPageSize())) {
            pageSize = PAGE_SIZE_DEFAULT;
        } else {
            pageSize = filters.getPageSize();
        }

        // 3. Produce regularized filter object
        OperatorSearchFilter regularizedFilter = filters.cloneBuilder()
                .withPageNum(pageNum)
                .withPageSize(pageSize)
                .build();

        // 4. Search through DAO
        int count = operatorDao.fetchOperatorTotalCountWithFilter(regularizedFilter);
        List<Operator> operators = operatorDao.fetchWithFilter(regularizedFilter);

        List<OperatorVO> operatorVOs = new ArrayList<>();
        for (Operator op : operators) {
            operatorVOs.add(convertOperatorToOperatorVO(op));
        }

        return PaginationVO.<OperatorVO>newBuilder()
                .withRecords(operatorVOs)
                .withPageNumber(pageNum)
                .withPageSize(pageSize)
                .withTotalCount(count)
                .build();
    }

    public List<Operator> fetchAllOperators() {
        return operatorDao.fetchWithFilter(
                OperatorSearchFilter.newBuilder()
                        .withPageSize(Integer.MAX_VALUE)
                        .withPageNum(1)
                        .build()
        );
    }

    public Optional<Operator> fetchOperatorById(Long id) {
        // 1. Id should not be null
        validateIdNotNull(id);

        // 2. Get optional operator through DAO
        return operatorDao.fetchById(id);
    }

    public Integer fetchOperatorTotalCount() {
        return operatorDao.fetchOperatorTotalCount();
    }

    public Integer fetchOperatorTotalCount(OperatorSearchFilter filter) {
        return operatorDao.fetchOperatorTotalCountWithFilter(filter);
    }

    /* ---------------------------------------- */
    /* ----------- private methods ------------ */
    /* ---------------------------------------- */

    @SuppressWarnings("unchecked")
    private Class<? extends KunOperator> loadOperatorClass0(String jarPath, String mainClass) {
        try {
            // TODO: 使用Resource接口读取Jar
            URL[] urls = {new URL("jar:" + jarPath + "!/")};
            URLClassLoader cl = URLClassLoader.newInstance(urls, getClass().getClassLoader());
            Class<?> clazz = Class.forName(mainClass, true, cl);
            if (!KunOperator.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException(mainClass + " is not a valid Operator class.");
            }
            return (Class<? extends KunOperator>) clazz;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load jar. jarPath=" + jarPath, e);
        }
    }

    private String packagePathForOperator(String libDirectory, Operator operator) {
        return String.join("/", libDirectory, operator.getId().toString(), operator.getName() + ".jar");
    }

    private void refreshOperatorCache(Long operatorId) {
        operatorCache.refresh(operatorId);
    }

    /**
     * URLClassLoader caches jar file with url path by default
     * and not clear the cache after the connection is closed
     * add dataTime at the end of jar path to avoid use cache
     * @param operator
     * @return
     */
    private String generateOperatorPath(Operator operator){
        String libDirectory = "file:" + props.get(PROP_RESOURCE_LIBDIRECTORY);
        return String.join("/", libDirectory, operator.getId().toString(), operator.getName() + "_" + DateTimeUtils.now());
    }

    public OperatorVO convertOperatorToOperatorVO(Operator operator) {
        OperatorVO vo = new OperatorVO();
        vo.setId(operator.getId());
        vo.setName(operator.getName());
        vo.setDescription(operator.getDescription());
        vo.setClassName(operator.getClassName());
        if (StringUtils.isNoneBlank(operator.getPackagePath())) {
            ConfigDef configDef = getOperatorConfigDef(operator.getId());
            vo.setConfigDef(configDef);
        }
        return vo;
    }

    private OperatorPropsVO convertOperatorToPropsVO(Operator operator) {
        return OperatorPropsVO.newBuilder()
                .withName(operator.getName())
                .withDescription(operator.getDescription())
                .withClassName(operator.getClassName())
                .build();
    }

    private void validatePropsVOIntegrity(OperatorPropsVO vo) {
        checkNotNull(vo, "Invalid parameter `vo`: found null object");
        Preconditions.checkArgument(StringUtils.isNotEmpty(vo.getName()), "Invalid OperatorInfoVO with empty `name`.");
        Preconditions.checkArgument(Objects.nonNull(vo.getDescription()), "Invalid OperatorInfoVO with null `description`.");
        Preconditions.checkArgument(StringUtils.isNotEmpty(vo.getClassName()), "Invalid OperatorInfoVO with empty `className`.");
    }

    private void validateIdNotNull(Long id) {
        checkNotNull(id, "Invalid id: null");
    }

    private void validateOperatorNotNull(Operator operator) {
        checkNotNull(operator, "Invalid Operator: null");
    }
}
