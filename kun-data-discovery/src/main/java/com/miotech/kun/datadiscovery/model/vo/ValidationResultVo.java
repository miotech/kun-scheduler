package com.miotech.kun.datadiscovery.model.vo;

import com.google.common.collect.Maps;
import com.miotech.kun.datadiscovery.model.entity.rdm.ValidationMessage;
import com.miotech.kun.datadiscovery.model.entity.rdm.ValidationResult;
import com.miotech.kun.datadiscovery.model.entity.rdm.ValidationType;
import lombok.Data;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-07-18 14:01
 **/
@Data
public class ValidationResultVo {
    private String summary;
    private List<ValidationMessageVo> validationMessageVoList;

    public ValidationResultVo(ValidationResult validationResult) {
        if (validationResult.getStatus()) {
            return;
        }
        this.summary = validationResult.getSummary();
        List<ValidationMessage> validationMessageList = validationResult.getValidationMessageList();
        HashMap<Long, ValidationMessageVo> map = Maps.newHashMap();
        validationMessageList.forEach(validationMessage -> mapping(map, validationMessage));
        validationMessageVoList = map.values().stream().filter(Objects::nonNull).sorted(Comparator.comparing(ValidationMessageVo::getLineNumber)).collect(Collectors.toList());
    }

    private void mapping(HashMap<Long, ValidationMessageVo> map, ValidationMessage validationMessage) {
        ValidationMessageVo validationMessageVo;
        Long lineNumber = validationMessage.getLineNumber();
        if (map.containsKey(lineNumber)) {
            validationMessageVo = map.get(lineNumber);
        } else {
            validationMessageVo = new ValidationMessageVo(lineNumber);
            map.put(lineNumber, validationMessageVo);
        }
        ValidationType validationType = validationMessage.getValidationType();
        switch (validationType) {
            case INCONSISTENT:
            case CONSTRAINT_PRIMARY_KEY:
                validationMessageVo.addMessage(validationMessage.getMessage());
                break;
            case DATA_FORMAT_ERROR:
                validationMessageVo.addCellMessage(validationMessage);
                break;
            default:
                throw new IllegalArgumentException(String.format("type does not exist:%s", validationType));

        }
    }

}
