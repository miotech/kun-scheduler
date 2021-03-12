package com.miotech.kun.dataplatform.notify;

public class NotifyLinkConfigContext {
    private final boolean enabled;

    private final String prefix;

    public NotifyLinkConfigContext(boolean enabled, String prefix) {
        this.enabled = enabled;
        this.prefix = prefix;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getPrefix() {
        return prefix;
    }
}
