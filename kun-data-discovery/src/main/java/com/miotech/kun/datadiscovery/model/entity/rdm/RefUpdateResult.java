package com.miotech.kun.datadiscovery.model.entity.rdm;

import com.miotech.kun.datadiscovery.model.vo.BaseRefTableVersionInfo;
import com.miotech.kun.datadiscovery.model.vo.ValidationResultVo;
import lombok.Data;

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
    private ValidationResultVo validationResultVo;


    private RefUpdateResult(boolean state, BaseRefTableVersionInfo baseRefTableVersionInfo, ValidationResultVo validationResultVo) {
        this.state = state;
        this.baseRefTableVersionInfo = baseRefTableVersionInfo;
        this.validationResultVo = validationResultVo;
    }

    public static RefUpdateResult success(BaseRefTableVersionInfo baseRefTableVersionInfo) {
        return new RefUpdateResult(true, baseRefTableVersionInfo, null);
    }

    public static RefUpdateResult error(ValidationResultVo validationResultVo) {
        return new RefUpdateResult(false, null, validationResultVo);
    }
}
