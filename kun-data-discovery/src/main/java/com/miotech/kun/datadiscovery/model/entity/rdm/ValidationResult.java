package com.miotech.kun.datadiscovery.model.entity.rdm;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-06-23 17:34
 **/

public class ValidationResult {
    private final CopyOnWriteArrayList<ValidationMessage> validationMessageList = Lists.newCopyOnWriteArrayList();
    private StringJoiner summary = new StringJoiner(",");

    public void addValidationMessage(ValidationMessage validationMessage) {
        validationMessageList.add(validationMessage);
    }

    public void addValidationMessage(List<ValidationMessage> list) {
        validationMessageList.addAll(list);
    }

    public Boolean getStatus() {
        return validationMessageList.isEmpty();
    }

    public List<ValidationMessage> getValidationMessageList() {
        return validationMessageList.stream().sorted(Comparator.comparing(ValidationMessage::getLineNumber)).collect(Collectors.toList());
    }

    public int size() {
        return validationMessageList.size();
    }

    public String getSummary() {
        return summary.toString();
    }

    public void addSummary(String info) {
        this.summary.add(info);
    }
}
