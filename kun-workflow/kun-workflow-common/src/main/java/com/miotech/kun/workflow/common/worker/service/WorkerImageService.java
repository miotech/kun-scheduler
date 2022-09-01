package com.miotech.kun.workflow.common.worker.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.workflow.common.task.vo.PaginationVO;
import com.miotech.kun.workflow.common.worker.dao.WorkerImageDao;
import com.miotech.kun.workflow.common.worker.filter.WorkerImageFilter;
import com.miotech.kun.workflow.core.model.worker.WorkerImage;

import java.util.List;

@Singleton
public class WorkerImageService {

    private final WorkerImageDao workerImageDao;

    @Inject
    public WorkerImageService(WorkerImageDao workerImageDao) {
        this.workerImageDao = workerImageDao;
    }

    public WorkerImage saveWorkerImage(WorkerImage workerImage) {
        Long imageId = IdGenerator.getInstance().nextId();
        WorkerImage toSaveImage = workerImage.cloneBuilder()
                .withId(imageId)
                .withActive(false)
                .build();
        return workerImageDao.saveWorkerImage(toSaveImage);
    }

    public PaginationVO<WorkerImage> fetchWorkerImage(WorkerImageFilter filter) {
        List<WorkerImage> workerImageList = workerImageDao.fetchWorkerImage(filter);
        return PaginationVO.<WorkerImage>newBuilder()
                .withPageNum(filter.getPage())
                .withPageSize(filter.getPageSize())
                .withRecords(workerImageList)
                .build();
    }

    public synchronized Boolean setActiveVersion(Long imageId, String imageName) {
        Boolean result = workerImageDao.setActiveVersion(imageId, imageName);
        return result;
    }

    public synchronized Boolean setActiveVersion(Long imageId) {
        WorkerImageFilter imageFilter = WorkerImageFilter.newBuilder()
                .withId(imageId)
                .withPage(0)
                .withPageSize(1)
                .build();
        WorkerImage workerImage = workerImageDao.fetchWorkerImage(imageFilter).get(0);
        return setActiveVersion(imageId, workerImage.getImageName());
    }

    /**
     * fetch Active image,
     * if never set an active image will return the latest one
     * @param imageName
     * @return
     */
    public WorkerImage fetchActiveImage(String imageName) {
        WorkerImageFilter activeFilter = WorkerImageFilter.newBuilder()
                .withPage(0)
                .withPageSize(1)
                .withName(imageName)
                .withActive(true)
                .build();
        List<WorkerImage> activeImage  = workerImageDao.fetchWorkerImage(activeFilter);
        if(activeImage.size() > 0){
            return activeImage.get(0);
        }
        WorkerImageFilter imageFilter = WorkerImageFilter.newBuilder()
                .withPage(0)
                .withPageSize(1)
                .withName(imageName)
                .build();
        List<WorkerImage> latestImage = workerImageDao.fetchWorkerImage(imageFilter);
        return latestImage.get(0);
    }

}
