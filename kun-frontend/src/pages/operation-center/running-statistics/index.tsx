import React, { memo, useEffect, useState, useCallback, useMemo } from 'react';
import { queryGanttTasks } from '@/services/gantt';
import { useHistory } from 'umi';

import { Filters } from '@/definitions/Gantt.type';
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
  const history = useHistory();
  const query = useMemo(() => (history.location as any)?.query ?? {}, [history.location]);
  const [filters, setFilters] = useState<Filters>({});
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
      taskRunId: query && query.taskRunId,
    };
    run(params);
  }, [query, run]);

  const onClickRefresh = () => {
    if (query && query.taskRunId) {
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
    if (query && query.taskRunId) {
      fetchUpstreamTask();
    } else {
      fetchTask();
    }
  }, [query, fetchTask, fetchUpstreamTask]);

  return (
    <div className={style.MainContent}>
      <ViewFilters
        filters={filters}
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
      {data && !loading && <Gantt data={data} taskRunId={query.taskRunId} />}
    </div>
  );
});

export default RunningStatistics;
