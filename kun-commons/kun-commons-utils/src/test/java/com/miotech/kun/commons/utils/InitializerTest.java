package com.miotech.kun.commons.utils;

import com.google.inject.*;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class InitializerTest {
    @Test
    public void initializeAfterPropertiesSet_regular_injection_success() {
        // process
        Injector injector = Guice.createInjector(
                new InitIndicatorModule(),
                new InitializeModule()
        );
        Initializer initializer = injector.getInstance(Initializer.class);
        initializer.initialize();

        // verify
        InitIndicator indicator = injector.getInstance(InitIndicator.class);
        assertThat(indicator.isInitialized(), is(true));
    }

    @Test
    public void initializeAfterPropertiesSet_jit_injection_should_fail() {
        // process
        Injector injector = Guice.createInjector(
                new InitializeModule()
        );
        Initializer initializer = injector.getInstance(Initializer.class);
        initializer.initialize();

        // verify
        InitIndicatorInjected holder = injector.getInstance(InitIndicatorInjected.class);
        assertThat(holder, is(notNullValue()));

        InitIndicator indicator = holder.getInitIndicator();
        assertThat(indicator.isInitialized(), is(false));
    }

    @Test
    public void initializeAfterPropertiesSet_initialize_by_order() {
        // process
        Injector injector = Guice.createInjector(
                new InitOrderedIndicatorModule(),
                new InitializeModule()
        );
        Initializer initializer = injector.getInstance(Initializer.class);
        initializer.initialize();

        // verify
        InitIndicatorFirst indicatorF = injector.getInstance(InitIndicatorFirst.class);
        InitIndicatorNormal indicatorN = injector.getInstance(InitIndicatorNormal.class);
        InitIndicatorLast indicatorL = injector.getInstance(InitIndicatorLast.class);

        assertThat(indicatorF.isInitialized(), is(true));
        assertThat(indicatorN.isInitialized(), is(true));
        assertThat(indicatorL.isInitialized(), is(true));

        assertThat(indicatorF.getInitTimestamp(), is(lessThan(indicatorN.getInitTimestamp())));
        assertThat(indicatorN.getInitTimestamp(), is(lessThan(indicatorL.getInitTimestamp())));
    }

    @Test
    public void initializer_be_able_to_initialize_only_once() {
        // process
        Injector injector = Guice.createInjector(
                new InitializeModule()
        );
        Initializer initializer = injector.getInstance(Initializer.class);
        boolean firstTime = initializer.initialize();
        boolean secondTime = initializer.initialize();

        // verify
        assertThat(firstTime, is(true));
        assertThat(secondTime, is(false));
    }

    private static class InitIndicatorModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(InitIndicator.class).toInstance(new InitIndicatorNormal());
        }
    }

    private static class InitOrderedIndicatorModule extends  AbstractModule {
        @Override
        protected void configure() {
            bind(InitIndicatorNormal.class).toInstance(new InitIndicatorNormal());
            bind(InitIndicatorLast.class).toInstance(new InitIndicatorLast());
            bind(InitIndicatorFirst.class).toInstance(new InitIndicatorFirst());
        }
    }

    @Singleton
    private static class InitIndicator implements InitializingBean {
        private boolean initialized = false;
        private long timestamp = 0L;

        public boolean isInitialized() {
            return initialized;
        }

        public long getInitTimestamp() {
            return timestamp;
        }

        @Override
        public void afterPropertiesSet() {
            initialized = true;
            timestamp = System.nanoTime();
        }
    }

    private static class InitIndicatorFirst extends InitIndicator {
        @Override
        public Order getOrder() {
            return Order.FIRST;
        }
    }

    private static class InitIndicatorNormal extends InitIndicator {
        @Override
        public Order getOrder() {
            return Order.NORMAL;
        }
    }

    private static class InitIndicatorLast extends InitIndicator {
        @Override
        public Order getOrder() {
            return Order.LAST;
        }
    }

    private static class InitIndicatorInjected {
        @Inject
        private InitIndicator indicator;

        public InitIndicator getInitIndicator() {
            return indicator;
        }
    }
}