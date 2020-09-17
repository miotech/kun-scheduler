package com.miotech.kun.metadata.databuilder.schedule;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.Props;

public class PropertiesModule extends AbstractModule {

    private final Props props;

    public PropertiesModule(Props props) {
        this.props = props;
    }

    @Provides
    @Singleton
    public Props buildProps() {
        return props;
    }

}
