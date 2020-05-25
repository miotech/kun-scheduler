package com.miotech.kun.datadiscover.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.datadiscover.constant.ErrorCode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author: Melo
 * @created: 5/26/20
 */

@EqualsAndHashCode(callSuper = false)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestResult<T> extends PageInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 返回代码
     */
    @JsonProperty("code")
    private Integer code;

    /**
     * 返回结果
     */
    @JsonProperty("note")
    private String note;

    /**
     * 返回数据对象
     */
    @JsonProperty("result")
    private T result;

    private RequestResult (int code, String note) {
        this.code = code;
        this.note = note;
    }

    public static <T> RequestResult<T> success() {
        return success(null);
    }

    public static <T> RequestResult<T> success(T resultObj) {
        return success(ErrorCode.SUCCESS.getNote(), resultObj);
    }

    public static <T> RequestResult<T> success(String note, T resultObject) {
        RequestResult<T> result = new RequestResult<>(ErrorCode.SUCCESS.getCode(), note);
        result.setResult(resultObject);
        return result;
    }

    public static <T> RequestResult<T> error() {
        return error(ErrorCode.FAILED.getCode(), ErrorCode.FAILED.getNote());
    }

    public static <T> RequestResult<T> error(String note) {
        return error(ErrorCode.FAILED.getCode(), note);
    }

    public static <T> RequestResult<T> error(int code, String note) {
        RequestResult<T> result = new RequestResult<>(code, note);
        return result;
    }
}