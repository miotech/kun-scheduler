package com.miotech.kun.workflow.common.lineage;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.metadata.facade.LineageServiceFacade;
import com.miotech.kun.metadata.facade.MetadataServiceFacade;
import com.miotech.kun.workflow.common.CommonTestBase;
import com.miotech.kun.workflow.common.lineage.service.LineageService;
import com.miotech.kun.workflow.common.operator.dao.OperatorDao;
import com.miotech.kun.workflow.common.task.service.TaskService;
import com.miotech.kun.workflow.common.task.vo.TaskPropsVO;
import com.miotech.kun.workflow.core.Scheduler;
import com.miotech.kun.workflow.core.model.lineage.UpstreamTaskBasicInformation;
import com.miotech.kun.workflow.core.model.lineage.UpstreamTaskInformation;
import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.testing.factory.MockOperatorFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import org.apache.commons.compress.utils.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import java.util.List;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;

public class LineageServiceTest extends CommonTestBase {

    @Inject
    private LineageService lineageService;

    @Inject
    private LineageServiceFacade lineageServiceFacade;

    @Inject
    private TaskService taskService;

    @Inject
    private OperatorDao operatorDao;

    @Override
    protected void configuration() {
        super.configuration();
        bind(LineageServiceFacade.class, Mockito.mock(LineageServiceFacade.class));
        bind(MetadataServiceFacade.class, Mockito.mock(MetadataServiceFacade.class));
        bind(Scheduler.class, Mockito.mock(Scheduler.class));
    }

    @Test
    public void testFetchDirectUpstreamTask_emptyArgs() {
        // mock
        doReturn(Lists.newArrayList()).when(lineageServiceFacade).fetchDirectUpstreamTask(ImmutableList.of());

        // execute
        List<UpstreamTaskInformation> upstreamTaskInformationList =
                lineageService.fetchDirectUpstreamTask(ImmutableList.of());

        // verify
        assertThat(upstreamTaskInformationList, empty());
    }

    @Test
    public void testFetchDirectUpstreamTask() {
        // mock
        Operator mockOperator = MockOperatorFactory.createOperator();
        operatorDao.create(mockOperator);

        TaskPropsVO taskPropsVO1 = MockTaskFactory.createTaskPropsVOWithOperator(mockOperator.getId());
        Task mockTask1 = taskService.createTask(taskPropsVO1);
        TaskPropsVO taskPropsVO2 = MockTaskFactory.createTaskPropsVOWithOperator(mockOperator.getId());
        Task mockTask2 = taskService.createTask(taskPropsVO2);

        UpstreamTaskBasicInformation mockUpstreamTaskInformation = new UpstreamTaskBasicInformation(IdGenerator.getInstance().nextId(),
                ImmutableList.of(mockTask1.getId(), mockTask2.getId()));
        List<UpstreamTaskBasicInformation> mockResult = ImmutableList.of(mockUpstreamTaskInformation);
        doReturn(mockResult).when(lineageServiceFacade).fetchDirectUpstreamTask(anyList());

        // execute
        List<UpstreamTaskInformation> upstreamTaskInformationList = lineageService.fetchDirectUpstreamTask(ImmutableList.of());

        // verify
        assertThat(upstreamTaskInformationList.size(), is(1));
        UpstreamTaskInformation upstreamTaskInformation = upstreamTaskInformationList.get(0);
        assertThat(upstreamTaskInformation.getDatasetGid(), is(mockUpstreamTaskInformation.getDatasetGid()));
        assertThat(upstreamTaskInformation.getTasks().size(), is(2));
        Task taskInformation1 = upstreamTaskInformation.getTasks().get(0);
        Task taskInformation2 = upstreamTaskInformation.getTasks().get(1);
        assertThat(taskInformation1, sameBeanAs(mockTask1));
        assertThat(taskInformation2, sameBeanAs(mockTask2));
    }

}
