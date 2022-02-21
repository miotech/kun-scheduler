import React, { memo, useEffect, useState, useCallback } from 'react';
import { queryGanttTasks } from '@/services/gantt';
import { Filters, TaskState } from '@/definitions/Gantt.type';
import { useRequest } from 'ahooks';
import moment from 'moment';
import { Card } from 'antd';
import { KunSpin } from '@/components/KunSpin';
import { ViewFilters } from './components/ViewFilters';
import { Gantt } from './components/Gantt';

import style from './index.less';

interface OwnProps {}

type Props = OwnProps;

export const RunningStatistics: React.FC<Props> = memo(function RunningStatistics() {
  const [filters, setFilters] = useState<Filters>({});
  const [task, setTask] = useState<TaskState>();
  const { data, loading, run } = useRequest(queryGanttTasks, {
    manual: true,
  });

  const fetchTask = useCallback(() => {
    const currentDayStartTime = moment()
      .startOf('day')
      .toISOString();
    const currentDayTime = moment()
      .endOf('day')
      .toISOString();

    const { timeType, startTime, endTime, taskRunId } = filters;
    const params = {
      startTime: startTime || currentDayStartTime,
      endTime: endTime || currentDayTime,
      timeType: timeType || 'createdAt',
      taskRunId: taskRunId || null,
    };

    run(params);
  }, [filters, run]);

  const fetchUpstreamTask = useCallback(() => {
    const params = {
      taskRunId: task && task.taskRunId,
    };
    run(params);
  }, [task, run]);

  const onClickRefresh = () => {
    if (task && task.taskRunId) {
      fetchUpstreamTask();
    } else {
      fetchTask();
    }
  };
  const updateFilters = (params: Filters) => {
    setFilters({
      ...filters,
      ...params,
    });
  };

  useEffect(() => {
    if (task && task.taskRunId) {
      fetchUpstreamTask();
    } else {
      fetchTask();
    }
  }, [task, fetchTask, fetchUpstreamTask]);

  return (
    <div className={style.MainContent}>
      <ViewFilters
        filters={filters}
        task={task}
        setTask={setTask}
        onClickRefresh={onClickRefresh}
        updateFilters={updateFilters}
        refreshBtnLoading={loading}
      />
      {loading && (
        <Card bodyStyle={{ padding: '8px' }}>
          <div>
            <KunSpin asBlock />
          </div>
        </Card>
      )}
      {data && !loading && <Gantt data={data} setTask={setTask} />}
    </div>
  );
});

export default RunningStatistics;
