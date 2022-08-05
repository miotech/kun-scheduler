package com.miotech.kun.datadiscovery.model.vo;

import com.miotech.kun.datadiscovery.model.entity.rdm.ValidationMessage;
import lombok.Data;
import org.apache.commons.compress.utils.Lists;

import java.util.List;
import java.util.Objects;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-07-18 14:15
 **/
@Data
public class ValidationMessageVo {
    private Long lineNumber;
    private String rowMessage;
    private List<ValidationMessage> cellMessageList;

    public ValidationMessageVo(Long lineNumber) {
        this.lineNumber = lineNumber;
    }

    public void addCellMessage(ValidationMessage validationMessage) {
        if (Objects.isNull(cellMessageList)) {
            cellMessageList = Lists.newArrayList();
        }
        cellMessageList.add(validationMessage);
    }

    public void addMessage(String message) {
        if (Objects.isNull(rowMessage)) {
            rowMessage = message;
            return;
        }
        rowMessage = rowMessage.concat(",").concat(message);
    }
}
