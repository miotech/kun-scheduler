package com.miotech.kun.metadata.core.model.constant;

/**
 * @program: kun
 * @description: search Operator
 * @author: zemin  huang
 * @create: 2022-03-13 19:55
 **/
public enum SearchOperator {
    AND("&"),
    OR("|");
  private String operator;

    SearchOperator(String operator) {
        this.operator = operator;
    }

    public String getOperator() {
        return operator;
    }
}
