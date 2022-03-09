package com.miotech.kun.metadata.core.model.constant;

/**
 * @program: kun
 * @description: search Type
 * @author: zemin  huang
 * @create: 2022-03-08 10:21
 **/
public enum SearchContent {
    NAME('A', 1.0),
    DESCRIPTION('B', 0.4),
    ATTRIBUTE('C', 0.2);
    private char weight;
    private double weightNum;

    SearchContent(char weight, double weightNum) {
        this.weight = weight;
        this.weightNum = weightNum;
    }

    public char getWeight() {
        return weight;
    }

    public void setWeight(char weight) {
        this.weight = weight;
    }

    public double getWeightNum() {
        return weightNum;
    }

    public void setWeightNum(double weightNum) {
        this.weightNum = weightNum;
    }
}
