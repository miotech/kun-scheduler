package com.miotech.kun.commons.utils;

import com.google.inject.AbstractModule;

public class InitializeModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Initializer.class);
    }
}
