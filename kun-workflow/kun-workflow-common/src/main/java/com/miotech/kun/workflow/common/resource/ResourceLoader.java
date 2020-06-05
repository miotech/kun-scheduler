package com.miotech.kun.workflow.common.resource;

import com.google.inject.ImplementedBy;

@ImplementedBy(ResourceLoaderImpl.class)
public interface ResourceLoader {
    /**
     * 通过资源路径获取资源。如果不存在，则抛错。
     */
    Resource getResource(String location);

    /**
     * 通过资源路径获取资源。如果不存在，则根据flag决定新建资源或抛错。
     */
    Resource getResource(String location, boolean createIfNotExist);
}
