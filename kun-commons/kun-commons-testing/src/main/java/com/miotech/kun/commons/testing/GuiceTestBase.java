package com.miotech.kun.commons.testing;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import org.junit.Before;
import org.mockito.Mockito;

import java.util.*;

public abstract class GuiceTestBase {
    private final List<Module> modules = new ArrayList<>();
    private final Map<Class, Object> mocks = new HashMap<>();

    protected Injector injector;

    @Before
    public void initInject() {
        // do custom configurations
        configuration();

        Module[] mods = modules.toArray(new Module[0]);
        injector = Guice.createInjector(Modules.override(mods).with(new InjectMockModule()));
        injector.injectMembers(this);
    }

    protected void addModules(Module... mods) {
        modules.addAll(Arrays.asList(mods));
    }

    protected <T> T mock(Class<T> clazz) {
        T mockObj = Mockito.mock(clazz);
        mocks.put(clazz, mockObj);
        return mockObj;
    }

    protected void configuration() {
        // do nothing
    }

    @SuppressWarnings("unchecked")
    private class InjectMockModule extends AbstractModule {
        @Override
        protected void configure() {
            for (Class clazz : mocks.keySet()) {
                bind(clazz).toInstance(mocks.get(clazz));
            }
        }
    }
}
