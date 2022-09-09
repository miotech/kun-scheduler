import React, { memo, useEffect, useState, useCallback } from 'react';
import { queryGanttTasks } from '@/services/gantt';
import { Filters } from '@/definitions/Gantt.type';
import { useRequest, useEventListener } from 'ahooks';
import moment from 'moment';
import { Card } from 'antd';
import { KunSpin } from '@/components/KunSpin';
import useUrlState from '@ahooksjs/use-url-state';
import { ViewFilters } from './components/ViewFilters';
import { Gantt } from './components/Gantt';
import { WaitTask } from './components/WaitTask';
import style from './index.less';
import SearchTaskModal from './components/SearchTaskModal';

interface OwnProps { }

type Props = OwnProps;

export const RunningStatistics: React.FC<Props> = memo(function RunningStatistics() {
  const [routeState] = useUrlState<any>({});
  const [filters, setFilters] = useState<Filters>({});
  const [currentIndex, setCurrentIndex] = useState(0);
  const [searchWords, setSearchWords] = useState('');
  const [searchModalVisible, setSearchModalVisible] = useState<boolean>(false);

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
      taskRunId: routeState?.taskRunId,
    };
    run(params);
  }, [routeState?.taskRunId, run]);

  const onClickRefresh = () => {
    if (routeState?.taskRunId) {
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


  const setScrollTo = useCallback((index) => {
    const scrollTop = index * 40 - 100 > 0 ? index * 40 - 100 : 0;
    setCurrentIndex(index);
    document.getElementById('bar').scrollTop = scrollTop;
  }, []);

  useEventListener('keydown', (e) => {
    const { ctrlKey, metaKey, key } = e;
    if (key === 'f' && (ctrlKey || metaKey)) {
      setSearchModalVisible(true);
      e.preventDefault();
      e.stopPropagation();
    }
  });
  useEffect(() => {
    if (routeState?.taskRunId) {
      fetchUpstreamTask();
    } else {
      fetchTask();
    }
  }, [routeState.taskRunId, fetchTask, fetchUpstreamTask]);

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
      {data && !loading &&
        <Gantt
          data={data}
          searchWords={searchWords}
          currentIndex={currentIndex}
          taskRunId={routeState?.taskRunId}
        />
      }
      <WaitTask drawerVisible={routeState?.waitForTaskRunId} waitForTaskRunId={routeState?.waitForTaskRunId} />
      {searchModalVisible &&
        <SearchTaskModal
          setSearchModalVisible={setSearchModalVisible}
          setSearchWords={setSearchWords}
          infoList={data?.infoList}
          setScrollTo={setScrollTo}
        />}
    </div>
  );
});

export default RunningStatistics;
