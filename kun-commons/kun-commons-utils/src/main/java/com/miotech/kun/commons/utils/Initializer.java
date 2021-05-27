package com.miotech.kun.commons.utils;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
public class Initializer {
    private static final Logger logger = LoggerFactory.getLogger(Initializer.class);

    private final AtomicBoolean invoked;

    private final Injector injector;

    @Inject
    public Initializer(Injector injector) {
        this.invoked = new AtomicBoolean(false);
        this.injector = injector;
    }

    public boolean initialize() {
        // invoke initialization only once
        if (!invoked.compareAndSet(false, true)) {
            return false;
        }

        List<InitializingBean> beans = scanInitializingBeans(injector);
        int count = beans.size();
        logger.debug("Found {} instances which need initialization.", count);

        for (InitializingBean bean : beans) {
            try {
                bean.afterPropertiesSet();
            } catch (Throwable e) {
                logger.error("Exception caught when running initializing method of {}", bean, e);
            }
        }
        logger.info("{} instances initialized.", count);

        return true;
    }

    private List<InitializingBean> scanInitializingBeans(Injector injector) {
        List<InitializingBean> beans = new ArrayList<>();
        for (Key<?> key : injector.getAllBindings().keySet()) {
            Class<?> type = key.getTypeLiteral().getRawType();
            if (InitializingBean.class.isAssignableFrom(type)) {
                InitializingBean obj = (InitializingBean) injector.getInstance(key);
                beans.add(obj);
            }
        }

        // sort them by order
        beans.sort(Comparator.comparing(InitializingBean::getOrder));

        return beans;
    }
}
