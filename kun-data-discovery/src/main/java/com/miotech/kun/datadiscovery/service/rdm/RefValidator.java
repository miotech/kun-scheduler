package com.miotech.kun.datadiscovery.service.rdm;

import com.google.common.collect.Lists;
import com.miotech.kun.datadiscovery.model.entity.rdm.RefBaseTable;
import com.miotech.kun.datadiscovery.model.entity.rdm.ValidationResult;
import org.springframework.stereotype.Component;

@Component
public class RefValidator {
    public ValidationResult valid(RefBaseTable refBaseTable) {
        return new ValidationResult(true, Lists.newArrayList());
    }
}
