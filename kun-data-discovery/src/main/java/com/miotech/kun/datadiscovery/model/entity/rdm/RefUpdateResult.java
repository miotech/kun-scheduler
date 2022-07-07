package com.miotech.kun.datadiscovery.model.entity.rdm;

import com.miotech.kun.datadiscovery.model.vo.BaseRefTableVersionInfo;
import lombok.Data;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-06-24 09:02
 **/
@Data
public final class RefUpdateResult {
    private boolean state;
    private BaseRefTableVersionInfo baseRefTableVersionInfo;
    private List<ValidationMessage> errorMessage;


    private RefUpdateResult(boolean state, BaseRefTableVersionInfo baseRefTableVersionInfo, List<ValidationMessage> errorMessage) {
        this.state = state;
        this.baseRefTableVersionInfo = baseRefTableVersionInfo;
        this.errorMessage = errorMessage;
    }

    public static RefUpdateResult success(BaseRefTableVersionInfo baseRefTableVersionInfo) {
        return new RefUpdateResult(true, baseRefTableVersionInfo, Lists.newArrayList());
    }

    public static RefUpdateResult error(List<ValidationMessage> errorMessage) {
        return new RefUpdateResult(false, null, errorMessage);
    }
}
