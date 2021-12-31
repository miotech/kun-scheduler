package com.miotech.kun.workflow.web.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.web.annotation.QueryParameter;
import com.miotech.kun.commons.web.annotation.RequestBody;
import com.miotech.kun.commons.web.annotation.RouteMapping;
import com.miotech.kun.workflow.common.task.vo.PaginationVO;
import com.miotech.kun.workflow.common.worker.filter.WorkerImageFilter;
import com.miotech.kun.workflow.common.worker.service.WorkerImageService;
import com.miotech.kun.workflow.core.model.worker.WorkerImage;

@Singleton
public class WorkerImageController {

    @Inject
    private WorkerImageService workerImageService;

    @RouteMapping(url= "/workers/image", method = "POST")
    public Object saveVersion(@RequestBody WorkerImage workerImage){
       return workerImageService.saveWorkerImage(workerImage);
    }

    @RouteMapping(url = "/workers/image", method = "GET")
    public PaginationVO<WorkerImage> searchWorkerImage(@QueryParameter Long id,
                                                       @QueryParameter String name,
                                                       @QueryParameter(defaultValue = "1") Integer page,
                                                       @QueryParameter(defaultValue = "10") Integer pageSize){
        WorkerImageFilter filter = WorkerImageFilter.newBuilder()
                .withId(id)
                .withName(name)
                .withPage(page - 1)
                .withPageSize(pageSize)
                .build();
        return workerImageService.fetchWorkerImage(filter);
    }

    @RouteMapping(url = "/workers/image/setActive", method = "PUT")
    public Object setActiveImage(@QueryParameter Long id){
        return workerImageService.setActiveVersion(id);
    }

}
