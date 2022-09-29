import React, { useState, useCallback } from 'react';
import { useRequest } from 'ahooks';
import { useParams } from 'react-router';

import useRedux from '@/hooks/useRedux';
import { fetchTaskDefinitionDetail } from '@/services/data-development/task-definitions';
import { fetchTaskRuns, FetchTaskRunsParams } from '@/services/task-tries/taskruns.service';
import { KunSpin } from '@/components/KunSpin';
import { FilterBar, TaskRunsTable, Filters } from './components';

import styles from './index.less';

const DryRunInstances: React.FC = () => {
  const { id: taskId } = useParams<{ id: string }>();
  const [query, setQuery] = useState<FetchTaskRunsParams>({
    pageNum: 1,
    pageSize: 20,
  });
  const [total, setTotal] = useState<number>(0);
  const [refresh, setRefresh] = useState<number>(0);
  const [tableData, setTableData] = useState<unknown[]>([]);

  const { dispatch } = useRedux(() => ({}));

  // Fetch task definition detail
  const { loading: taskDetailIsLoading } = useRequest(fetchTaskDefinitionDetail.bind(null, taskId), {
    refreshDeps: [taskId],
    onSuccess: taskDetail => {
      dispatch.route.updateCurrentParams({
        id: taskId,
        taskName: taskDetail?.name,
      });
    },
  });

  const { loading: instancesIsLoading } = useRequest(fetchTaskRuns.bind(null, taskId, query), {
    refreshDeps: [refresh, query],
    onSuccess: data => {
      const { totalCount, records } = data || {};
      setTableData((records ?? []).map(item => ({ ...item, key: item?.id ?? item })));
      setTotal(totalCount ?? 0);
    },
  });

  const onPaginationChange = useCallback(
    (pageNum: number, pageSize: number) => {
      setQuery({ ...query, pageNum, pageSize });
    },
    [query],
  );
  const handleFilterChange = useCallback(
    (filters: Filters) => {
      setQuery({ ...query, ...filters, pageNum: 1 });
    },
    [query],
  );

  const handleRefresh = useCallback(() => {
    setRefresh(refresh + 1);
  }, [refresh]);

  const handleTaskStop = useCallback(() => {
    handleRefresh();
  }, [handleRefresh]);

  const handleTaskRerun = useCallback(() => {
    handleRefresh();
  }, [handleRefresh]);

  return (
    <KunSpin spinning={taskDetailIsLoading}>
      <div className={styles.View}>
        <FilterBar
          taskDefId={taskId}
          status={query.status}
          onFilterChange={handleFilterChange}
          onRefresh={handleRefresh}
          loading={instancesIsLoading}
        />
        <TaskRunsTable
          loading={instancesIsLoading}
          total={total}
          pageNum={query.pageNum}
          pageSize={query.pageSize}
          onPaginationChange={onPaginationChange}
          data={tableData}
          onTaskStop={handleTaskStop}
          onTaskRerun={handleTaskRerun}
        />
      </div>
    </KunSpin>
  );
};

export default DryRunInstances;
