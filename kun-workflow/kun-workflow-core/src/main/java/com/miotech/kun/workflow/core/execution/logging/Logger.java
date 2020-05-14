package com.miotech.kun.workflow.core.execution.logging;

public interface Logger {

    public void debug(String format, Object... arguments);

    public void info(String format, Object... arguments);

    public void warn(String format, Object... arguments);

    public void error(String format, Object... arguments);
}
