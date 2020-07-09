package com.miotech.kun.metadata.web;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.miotech.kun.commons.web.annotation.BasePackageScan;

public class PackageScanModule extends AbstractModule {

    @Provides
    @Singleton
    @BasePackageScan
    public String getPackageScan() {
        return this.getClass().getPackage().getName();
    }

}
