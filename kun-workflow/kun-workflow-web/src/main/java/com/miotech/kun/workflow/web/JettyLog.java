package com.miotech.kun.workflow.web;

import org.eclipse.jetty.util.log.Logger;

public class JettyLog implements Logger {
    @Override public String getName() { return "jetty info log"; }
    @Override public void warn(String msg, Object... args) {
        //do nothing
    }
    @Override public void warn(Throwable thrown) {
        //do nothing
    }
    @Override public void warn(String msg, Throwable thrown) {
        //do nothing
    }
    @Override public void info(String msg, Object... args) {
        //do nothing
    }
    @Override public void info(Throwable thrown) {
        //do nothing
    }
    @Override public void info(String msg, Throwable thrown) {
        //do nothing
    }
    @Override public boolean isDebugEnabled() { return false; }
    @Override public void setDebugEnabled(boolean enabled) {
        //do nothing
    }
    @Override public void debug(String msg, Object... args) {
        //do nothing
    }

    @Override
    public void debug(String msg, long value) {
        //do nothing
    }

    @Override public void debug(Throwable thrown) {
        //do nothing
    }
    @Override public void debug(String msg, Throwable thrown) {
        //do nothing
    }
    @Override public Logger getLogger(String name) { return this; }
    @Override public void ignore(Throwable ignored) {
        //do nothing
    }
}
