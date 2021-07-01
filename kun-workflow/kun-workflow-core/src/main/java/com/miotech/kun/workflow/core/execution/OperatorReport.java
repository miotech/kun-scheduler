package com.miotech.kun.workflow.core.execution;

import com.miotech.kun.metadata.core.model.dataset.DataStore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OperatorReport implements Serializable {

    private  List<DataStore> inlets = new ArrayList<>();
    private  List<DataStore> outlets = new ArrayList<>();

    public static final OperatorReport BLANK = new OperatorReport();

    public List<DataStore> getInlets() {
        return inlets;
    }

    public void setInlets(List<DataStore> inlets) {
        this.inlets = copyList(inlets);
    }

    public List<DataStore> getOutlets() {
        return outlets;
    }

    public void setOutlets(List<DataStore> outlets) {
        this.outlets = copyList(outlets);
    }

    private List<DataStore> copyList(List<DataStore> source){
        List<DataStore> copy = new ArrayList<>();
        copy.addAll(source);
        return copy;
    }

    public void copyFromReport(TaskAttemptReport taskAttemptReport){
        setInlets(taskAttemptReport.getInlets());
        setOutlets(taskAttemptReport.getOutlets());
    }
}
