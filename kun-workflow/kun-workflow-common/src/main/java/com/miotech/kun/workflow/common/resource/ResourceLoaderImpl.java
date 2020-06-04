package com.miotech.kun.workflow.common.resource;

import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

@Singleton
public class ResourceLoaderImpl implements ResourceLoader {

    private final Logger logger = LoggerFactory.getLogger(ResourceLoaderImpl.class);

    @Override
    public Resource getResource(String location) {
        return getResource(location, false);
    }

    @Override
    public Resource getResource(String location, boolean createIfNotExist) {
        logger.debug("Get resource of {}", location);

        try {
            URI uri = new URI(location);
            String scheme = uri.getScheme();
            if (StringUtils.isAnyEmpty(scheme)) {
                logger.debug("Cannot refer the resource schema, use file as default");
                scheme = "file";
            }
            switch (scheme) {
                case FileResource.FILE_SCHEME:
                    return new FileResource(uri.getRawPath(), createIfNotExist);
                default:
                    throw new RuntimeException("Unsupported resource type: " + scheme);
            }
        } catch (URISyntaxException e) {
            logger.error("Invalid resource uri: {}", location, e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }
}
