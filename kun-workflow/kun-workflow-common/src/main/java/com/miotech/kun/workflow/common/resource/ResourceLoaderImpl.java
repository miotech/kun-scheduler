package com.miotech.kun.workflow.common.resource;

import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.workflow.core.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

@Singleton
public class ResourceLoaderImpl implements ResourceLoader {
    private static final Logger logger = LoggerFactory.getLogger(ResourceLoaderImpl.class);

    @Override
    public Resource getResource(String location) {
        return getResource(location, false);
    }

    @Override
    public Resource getResource(String location, boolean createIfNotExists) {
        logger.debug("Get resource of {}", location);

        try {
            URL url = new URL(new URL("file://"), location);
            ResourceType scheme = ResourceType.resolve(url.getProtocol());
            if (scheme == null) {
                throw new IllegalArgumentException("Unresolved resource type: " + url.toString());
            }

            switch (scheme) {
                case FILE:
                    return new FileResource(url.getPath(), createIfNotExists);
                default:
                    throw new IllegalArgumentException("Unsupported resource type: " + scheme);
            }
        } catch (MalformedURLException e) {
            logger.error("Invalid resource url: {}", location, e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }
}
