package com.miotech.kun.monitor.sla.timeline.dao;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.monitor.sla.MonitorSlaTestBase;
import com.miotech.kun.monitor.sla.common.dao.TaskTimelineDao;
import com.miotech.kun.monitor.sla.mocking.MockTaskTimelineFactory;
import com.miotech.kun.monitor.sla.model.BacktrackingTaskDefinition;
import com.miotech.kun.monitor.sla.model.TaskTimeline;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TaskTimelineDaoTest extends MonitorSlaTestBase {

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
        assertThat(taskTimelineOfFetch.getRootDefinitionId(), nullValue());
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

    @Test
    public void testFetchBacktrackingByDefinitionId() {
        // prepare
        Long definitionId = IdGenerator.getInstance().nextId();
        Long rootDefinitionId1 = IdGenerator.getInstance().nextId();
        Long rootDefinitionId2 = IdGenerator.getInstance().nextId();
        TaskTimeline taskTimeline = MockTaskTimelineFactory.createTaskTimeline(definitionId);
        taskTimelineDao.create(taskTimeline);
        TaskTimeline taskTimeline1 = MockTaskTimelineFactory.createTaskTimeline(definitionId, rootDefinitionId1);
        taskTimelineDao.create(taskTimeline1);
        TaskTimeline taskTimeline2 = MockTaskTimelineFactory.createTaskTimeline(definitionId, rootDefinitionId2);
        taskTimelineDao.create(taskTimeline2);

        BacktrackingTaskDefinition backtrackingTaskDefinition = taskTimelineDao.fetchBacktrackingByDefinitionId(definitionId);
        assertThat(backtrackingTaskDefinition.getDefinitionId(), is(rootDefinitionId2));
    }

}
