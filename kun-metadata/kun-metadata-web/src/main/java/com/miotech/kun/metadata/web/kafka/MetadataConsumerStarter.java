package com.miotech.kun.metadata.web.kafka;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class MetadataConsumerStarter {

    private MetadataChangeEventsProcessor mceProcessor;

    @Inject
    public MetadataConsumerStarter(MetadataChangeEventsProcessor mceProcessor) {
        this.mceProcessor = mceProcessor;
    }

    public void start() {
        mceProcessor.start();
    }

}
