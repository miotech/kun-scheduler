package com.miotech.kun.workflow.common.worker;

import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.workflow.common.task.vo.PaginationVO;
import com.miotech.kun.workflow.common.worker.dao.WorkerImageDao;
import com.miotech.kun.workflow.common.worker.filter.WorkerImageFilter;
import com.miotech.kun.workflow.common.worker.service.WorkerImageService;
import com.miotech.kun.workflow.core.model.worker.WorkerImage;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class WorkerImageServiceTest extends DatabaseTestBase {

    @Inject
    private WorkerImageService workerImageService;

    @Inject
    private WorkerImageDao workerImageDao;


    @Test
    public void setActiveImage_should_cancel_old_active_version(){
        //prepare
        WorkerImage activeImage = WorkerImage.newBuilder()
                .withId(IdGenerator.getInstance().nextId())
                .withImageName("test")
                .withVersion("v1")
                .withActive(true)
                .build();
        workerImageDao.saveWorkerImage(activeImage);
        WorkerImage newImage = WorkerImage.newBuilder()
                .withImageName("test")
                .withVersion("v2")
                .build();
        WorkerImage savedNewImage  = workerImageService.saveWorkerImage(newImage);

        workerImageService.setActiveVersion(savedNewImage.getId());

        //verify
        WorkerImage currentActive = workerImageService.fetchActiveImage("test");

        assertThat(currentActive.getId(),is(savedNewImage.getId()));
        assertThat(currentActive.getActive(),is(true));

    }

    @Test
    public void fetchActiveImage_not_set_active_should_return_latest(){
        //prepare
        WorkerImage image1 = WorkerImage.newBuilder()
                .withImageName("test")
                .withVersion("v1")
                .build();
        workerImageService.saveWorkerImage(image1);
        WorkerImage image2 = WorkerImage.newBuilder()
                .withImageName("test")
                .withVersion("v2")
                .build();
        workerImageService.saveWorkerImage(image2);
        WorkerImage image3 = WorkerImage.newBuilder()
                .withImageName("test")
                .withVersion("v3")
                .build();
        WorkerImage saved3 = workerImageService.saveWorkerImage(image3);

        WorkerImage activeImage = workerImageService.fetchActiveImage("test");

        assertThat(activeImage.getId(),is(saved3.getId()));
        assertThat(activeImage.getVersion(),is("v3"));
        assertThat(activeImage.getActive(),is(false));


    }

    @Test
    public void fetchImageWithFilter_should_return_expected(){

        //prepare
        WorkerImage image1 = WorkerImage.newBuilder()
                .withImageName("test")
                .withVersion("v1")
                .build();
        WorkerImage saved1 = workerImageService.saveWorkerImage(image1);
        WorkerImage image2 = WorkerImage.newBuilder()
                .withImageName("test")
                .withVersion("v2")
                .build();
        workerImageService.saveWorkerImage(image2);
        WorkerImage image3 = WorkerImage.newBuilder()
                .withImageName("test")
                .withVersion("v3")
                .build();
        workerImageService.saveWorkerImage(image3);

        WorkerImageFilter filter = WorkerImageFilter.newBuilder()
                .withId(saved1.getId())
                .withPage(0)
                .withPageSize(1)
                .build();

        PaginationVO<WorkerImage> fetched = workerImageService.fetchWorkerImage(filter);

        //verify
        List<WorkerImage> workerImageList = fetched.getRecords();
        assertThat(workerImageList,hasSize(1));
        assertThat(workerImageList.get(0).getId(),is(saved1.getId()));

    }

}
