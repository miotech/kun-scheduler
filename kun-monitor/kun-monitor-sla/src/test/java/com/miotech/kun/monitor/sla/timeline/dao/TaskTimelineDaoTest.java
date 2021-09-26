package com.miotech.kun.monitor.sla.timeline.dao;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.monitor.sla.AppTestBase;
import com.miotech.kun.monitor.sla.common.dao.TaskTimelineDao;
import com.miotech.kun.monitor.sla.mocking.MockTaskTimelineFactory;
import com.miotech.kun.monitor.sla.model.TaskTimeline;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TaskTimelineDaoTest extends AppTestBase {

    @Autowired
    private TaskTimelineDao taskTimelineDao;

    @Test
    public void testCreateSuccessThenFetch() {
        Long definitionId = IdGenerator.getInstance().nextId();
        TaskTimeline taskTimeline = MockTaskTimelineFactory.createTaskTimeline(definitionId);
        taskTimelineDao.create(taskTimeline);

        List<TaskTimeline> taskTimelines = taskTimelineDao.fetchByDefinitionId(definitionId);
        assertThat(taskTimelines.size(), is(1));
        TaskTimeline taskTimelineOfFetch = taskTimelines.get(0);
        assertThat(taskTimelineOfFetch, sameBeanAs(taskTimeline).ignoring("id").ignoring("rootDefinitionId"));
        assertThat(taskTimelineOfFetch.getRootDefinitionId(), is(0L));
    }

    @Test
    public void testCreateWithRootDefinitionIdSuccessThenFetch() {
        Long definitionId = IdGenerator.getInstance().nextId();
        TaskTimeline taskTimeline = MockTaskTimelineFactory.createTaskTimeline(definitionId, definitionId);
        taskTimelineDao.create(taskTimeline);

        List<TaskTimeline> taskTimelines = taskTimelineDao.fetchByDefinitionId(definitionId);
        assertThat(taskTimelines.size(), is(1));
        TaskTimeline taskTimelineOfFetch = taskTimelines.get(0);
        assertThat(taskTimelineOfFetch, sameBeanAs(taskTimeline).ignoring("id"));
    }

}
