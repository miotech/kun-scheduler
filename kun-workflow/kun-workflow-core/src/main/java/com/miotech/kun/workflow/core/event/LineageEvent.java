package com.miotech.kun.workflow.core.event;

import com.miotech.kun.workflow.core.model.lineage.DataStore;

public class LineageEvent extends Event{
    private DataStore inlets;
    private DataStore outlets;

    public LineageEvent(DataStore inlets, DataStore outlets){
        this.inlets = inlets;
        this.outlets = outlets;
    }

    public DataStore getInlets() {
        return inlets;
    }

    public void setInlets(DataStore inlets) {
        this.inlets = inlets;
    }

    public DataStore getOutlets() {
        return outlets;
    }

    public void setOutlets(DataStore outlets) {
        this.outlets = outlets;
    }

}
