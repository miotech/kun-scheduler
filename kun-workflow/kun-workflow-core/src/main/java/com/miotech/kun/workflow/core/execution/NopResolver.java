package com.miotech.kun.workflow.core.execution;

import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.workflow.core.annotation.Internal;

import java.util.LinkedList;
import java.util.List;

/**
 * A resolver that always resolves empty upstream and downstream. Internal use only.
 * DO NOT USE IT IN ANY OPERATOR CLASSES IN PRODUCTION.
 */
@Internal
public class NopResolver implements Resolver {
    @Override
    public List<DataStore> resolveUpstreamDataStore(Config config) {
        return new LinkedList<>();
    }

    @Override
    public List<DataStore> resolveDownstreamDataStore(Config config) {
        return new LinkedList<>();
    }
}
