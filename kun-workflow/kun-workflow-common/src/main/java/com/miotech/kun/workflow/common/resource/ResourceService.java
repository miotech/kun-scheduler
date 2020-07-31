package com.miotech.kun.workflow.common.resource;

import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.workflow.core.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Singleton
public class ResourceService {
    private static final Logger logger = LoggerFactory.getLogger(ResourceService.class);

    @Inject
    private ResourceLoader resourceLoader;

    public Resource createResource(String location, InputStream inputStream) {
        Resource resource = resourceLoader.getResource(location, true);

        try (OutputStream outputStream = resource.getOutputStream()) {
            ByteStreams.copy(inputStream, outputStream);
            return resource;
        } catch (IOException e) {
            logger.error("failed to crate resource {}", e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }
}
