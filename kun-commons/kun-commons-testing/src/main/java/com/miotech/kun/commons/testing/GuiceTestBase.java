package com.miotech.kun.commons.testing;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import org.junit.Before;
import org.mockito.Mockito;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class GuiceTestBase {

    private final EmbeddedModule ebdMod = new EmbeddedModule();
    private final Map<Object, Object> mocks = new HashMap<>();
    private final Map<Object, Object> bindings = new HashMap<>();
    private final List<Module> modules = Lists.newArrayList(ebdMod);

    protected Injector injector;

    @Before
    public void initInject() {
        // do custom configurations
        configuration();

        Module[] mods = modules.toArray(new Module[0]);
        injector = Guice.createInjector(Modules.override(mods).with(new InjectMockModule()));
        injector.injectMembers(this);

        Unsafe.setInjector(injector);
    }

    protected void addModules(Module... mods) {
        modules.addAll(Arrays.asList(mods));
    }

    /**
     * Create a mock bean that injected everywhere
     * @param clazz
     * @param <T>
     * @return
     */
    protected <T> T mock(Class<T> clazz) {
        T mockObj = Mockito.mock(clazz);
        mocks.put(clazz, mockObj);
        return mockObj;
    }

    /**
     * Create a spy bean that injected everywhere
     * @param target
     * @param <T>
     * @return
     */
    protected <T> T spy(T target) {
        // FIXME: spy() isn't a plain proxy
        return Mockito.spy(target);
    }

    protected <T> void bind(Class<? super T> clazz, Class<T> clazz2) {
        bindings.put(clazz, clazz2);
    }

    protected <T> void bind(Class<? super T> clazz, T instance) {
        bindings.put(clazz, instance);
    }

    protected void configuration() {
        // do nothing
    }

    private class EmbeddedModule extends AbstractModule {
        @Override
        protected void configure() {
            for (Object key : bindings.keySet()) {
                Object val = bindings.get(key);
                if (isInterfaceToImplementation(key, val)) {
                    bind((Class) key).to((Class) val);
                } else if (isInterfaceToInstance(key, val)) {
                    bind((Class) key).toInstance(val);
                } else {
                    throw new UnsupportedOperationException("Not supported yet. Please implement it yourself.");
                }
            }
        }
    }

    private class InjectMockModule extends AbstractModule {
        @Override
        protected void configure() {
            for (Object key : mocks.keySet()) {
                if (key instanceof Class) {
                    bind((Class<Object>) key).toInstance(mocks.get(key));
                } else {
                    throw new UnsupportedOperationException("Unsupported binding key: " + key);
                }
            }
        }
    }

    private boolean isInterfaceToImplementation(Object key, Object value) {
        return key instanceof Class && value instanceof Class && ((Class) key).isAssignableFrom((Class) value);
    }

    private boolean isInterfaceToInstance(Object key, Object value) {
        return key instanceof Class && ((Class) key).isAssignableFrom(value.getClass());
    }
}
