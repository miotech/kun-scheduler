package com.miotech.kun.common.operator.service;

import com.google.common.base.Preconditions;
import com.miotech.kun.common.exception.RuleOperatorInUseException;
import com.miotech.kun.common.operator.dao.OperatorDao;
import com.miotech.kun.common.operator.vo.OperatorPropsVO;
import com.miotech.kun.common.exception.DuplicatedNameException;
import com.miotech.kun.common.exception.EntityNotFoundException;
import com.miotech.kun.common.operator.filter.OperatorSearchFilter;
import com.miotech.kun.common.task.dao.TaskDao;
import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Singleton
public class OperatorService {
    @Inject
    private OperatorDao operatorDao;

    @Inject
    private TaskDao taskDao;

    private static final int PAGE_NUM_DEFAULT = 1;

    private static final int PAGE_SIZE_DEFAULT = Integer.MAX_VALUE;

    private Operator convertPropsVOToOperator(OperatorPropsVO vo) {
        return Operator.newBuilder()
                .withName(vo.getName())
                .withDescription(vo.getDescription())
                .withClassName(vo.getClassName())
                .withPackagePath(vo.getPackagePath())
                .withParams(vo.getParams())
                .build();
    }

    private Operator convertPropsVOToOperatorWithId(OperatorPropsVO vo, Long id) {
        return Operator.newBuilder()
                .withId(id)
                .withName(vo.getName())
                .withDescription(vo.getDescription())
                .withClassName(vo.getClassName())
                .withPackagePath(vo.getPackagePath())
                .withParams(vo.getParams())
                .build();
    }

    private OperatorPropsVO convertOperatorToPropsVO(Operator operator) {
        return OperatorPropsVO.newBuilder()
                .withName(operator.getName())
                .withDescription(operator.getDescription())
                .withClassName(operator.getClassName())
                .withPackagePath(operator.getPackagePath())
                .withParams(operator.getParams())
                .build();
    }

    private void validatePropsVOIntegrity(OperatorPropsVO vo) {
        Preconditions.checkNotNull(vo, "Invalid parameter `vo`: found null object");
        Preconditions.checkArgument(StringUtils.isNotEmpty(vo.getName()), "Invalid OperatorInfoVO with empty `name`.");
        Preconditions.checkArgument(Objects.nonNull(vo.getDescription()), "Invalid OperatorInfoVO with null `description`.");
        Preconditions.checkArgument(StringUtils.isNotEmpty(vo.getClassName()), "Invalid OperatorInfoVO with empty `className`.");
        Preconditions.checkArgument(StringUtils.isNotEmpty(vo.getPackagePath()), "Invalid OperatorInfoVO with empty `packagePath`.");
    }

    private void validateIdNotNull(Long id) {
        Preconditions.checkNotNull(id, "Invalid id: null");
    }

    private void validateOperatorNotNull(Operator operator) {
        Preconditions.checkNotNull(operator, "Invalid Operator: null");
    }

    public Operator createOperator(OperatorPropsVO vo) {
        // 1. Check integrity of operator properties VO
        validatePropsVOIntegrity(vo);

        // 2. Operator name should not be duplicated
        if (operatorDao.fetchByName(vo.getName()).isPresent()) {
            throw new DuplicatedNameException(String.format("Cannot create operator with duplicated name: \"%s\"", vo.getName()));
        }

        // 3. Assign operator id and generate Operator instance
        Long id = WorkflowIdGenerator.nextOperatorId();
        Operator operator = convertPropsVOToOperator(vo)
                .cloneBuilder()
                .withId(id)
                .build();

        // 4. Persist it and return
        operatorDao.create(operator);
        Optional<Operator> operatorOptional = operatorDao.fetchById(id);
        return operatorOptional.orElse(null);
    }

    /**
     * Persist an newly created operator object, or update an existing operator.
     * If `id` of `operator` object is not initialized, a generated id will be automatically assigned.
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
                throw new DuplicatedNameException(String.format("Cannot update operator with conflict name: \"%s\"", vo.getName()));
            }
        }

        // 4. Update existed
        Operator operator = convertPropsVOToOperator(vo);
        operatorDao.updateById(operatorId, operator);
        return operator;
    }

    public Operator partialUpdateOperator(Long operatorId, OperatorPropsVO vo) {
        // 1. Validate arguments
        validateIdNotNull(operatorId);
        Preconditions.checkNotNull(vo, "Cannot perform update with vo: null");

        // 2. If operator not exists, throw exception
        Optional<Operator> fetchedOperator = operatorDao.fetchById(operatorId);
        if (!fetchedOperator.isPresent()) {
            throw new EntityNotFoundException(String.format("Cannot perform update on non-exist operator with id: %d", operatorId));
        }

        // 3. produce a "partially" updated operator from existing one as template
        Operator operator = fetchedOperator.get();
        Operator updatedOperator = operator.cloneBuilder()
                .withName(StringUtils.isEmpty(vo.getName()) ? operator.getName() : vo.getName())
                .withDescription(StringUtils.isEmpty(vo.getDescription()) ? operator.getDescription() : vo.getDescription())
                .withClassName(StringUtils.isEmpty(vo.getClassName()) ? operator.getClassName() : vo.getClassName())
                .withPackagePath(StringUtils.isEmpty(vo.getPackagePath()) ? operator.getPackagePath() : vo.getPackagePath())
                .withParams(CollectionUtils.isEmpty(vo.getParams()) ? operator.getParams() : vo.getParams())
                .build();

        // 4. update through DAO
        operatorDao.updateById(operatorId, updatedOperator);
        return updatedOperator;
    }

    /**
     * Delete an operator.
     * Note that if there is any usage from task, a RuleOperatorInUseException will be thrown.
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
        List<Task> relatedTasks = taskDao.fetchWithOperatorId(id);
        if (CollectionUtils.isNotEmpty(relatedTasks)) {
            throw new RuleOperatorInUseException(String.format(
                    "Operator with id \"%d\" is used by %d tasks already. Remove these tasks first or replace internal operators by another operator.",
                    id,
                    relatedTasks.size()
            ));
        }

        // 4. delete operator by DAO
        operatorDao.deleteById(id);
    }

    public List<Operator> searchOperators(OperatorSearchFilter filters) {
        // 1. Filter should not be null
        Preconditions.checkNotNull(filters, "Cannot search operators with filter: null");

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
        return operatorDao.search(regularizedFilter);
    }

    public List<Operator> getAllOperators() {
        return operatorDao.search(
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
}
