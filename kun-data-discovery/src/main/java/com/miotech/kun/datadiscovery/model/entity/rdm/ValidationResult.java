package com.miotech.kun.datadiscovery.model.entity.rdm;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-06-23 17:34
 **/
@Data
@AllArgsConstructor
public class ValidationResult {
    private final Boolean status;
    private final List<ValidationMessage> validationMessageList;

    public void setValidationMessage(ValidationMessage validationMessage) {
        validationMessageList.add(validationMessage);
    }

}
