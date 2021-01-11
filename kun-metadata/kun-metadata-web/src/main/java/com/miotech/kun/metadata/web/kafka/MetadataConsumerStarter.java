package com.miotech.kun.metadata.web.kafka;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class MetadataConsumerStarter {

    private MetadataChangeEventsProcessor mceProcessor;
    private MetadataStatEventsProcessor mseProcessor;

    @Inject
    public MetadataConsumerStarter(MetadataChangeEventsProcessor mceProcessor, MetadataStatEventsProcessor mseProcessor) {
        this.mceProcessor = mceProcessor;
        this.mseProcessor = mseProcessor;
    }

    public void start() {
        mceProcessor.start();
        mseProcessor.start();
    }

}
