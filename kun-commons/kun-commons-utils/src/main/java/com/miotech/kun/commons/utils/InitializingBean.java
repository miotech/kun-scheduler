package com.miotech.kun.commons.utils;

public interface InitializingBean {
    /**
     * Get the initializing order.
     */
    default Order getOrder() {
        return Order.NORMAL;
    }

    /**
     * Invoked after instance is constructed and injected. Similar to Spring's counterpart.
     * <br/>
     * Be aware that JIT Bindings are not eligible for initialization, you need to bind it explicitly.
     */
    void afterPropertiesSet();

    enum Order {
        FIRST,
        NORMAL,
        LAST;
    }
}
