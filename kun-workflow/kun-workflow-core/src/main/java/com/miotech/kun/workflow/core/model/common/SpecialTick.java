package com.miotech.kun.workflow.core.model.common;

public class SpecialTick extends Tick {

    public final static SpecialTick DIRECTLY_TICK = new SpecialTick("197001010000",TickType.DIRECTLY);

    private final TickType type;

    public SpecialTick(String dateTime) {
        this(dateTime, TickType.UNDEFINE);
    }

    public SpecialTick(String dateTime, TickType tickType) {
        super(dateTime);
        this.type = tickType;
    }

    public Tick toTick(){
        return new Tick(getTime());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
