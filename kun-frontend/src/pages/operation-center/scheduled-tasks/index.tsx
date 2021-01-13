import React, { useEffect, useState } from 'react';
import { Card } from 'antd';
import { useSize, useUnmount, useUpdateEffect } from 'ahooks';
import useRedux from '@/hooks/useRedux';
import useDebouncedUpdateEffect from '@/hooks/useDebouncedUpdateEffect';

import { DeployedTasksTable } from '@/pages/operation-center/scheduled-tasks/components/DeployedTasksTable';
import { DeployedTask } from '@/definitions/DeployedTask.type';
import DeployedTaskDAG from '@/pages/operation-center/scheduled-tasks/components/DeployedTaskDAG';
import { Headings } from './components/Headings';

import styles from './index.less';

interface ScheduledTaskViewProps extends React.ComponentProps<'div'> {}

export const ScheduledTaskView: React.FC<ScheduledTaskViewProps> = () => {
  const {
    selector: { shouldRefresh, filters },
    dispatch,
  } = useRedux(s => ({
    shouldRefresh: s.scheduledTasks.shouldRefresh,
    filters: s.scheduledTasks.filters,
    loading: s.loading.effects.scheduledTasks.fetchScheduledTasks,
  }));

  const [selectedTask, setSelectedTask] = useState<DeployedTask | null>(null);

  useEffect(() => {
    if (shouldRefresh) {
      dispatch.scheduledTasks.fetchScheduledTasks(filters);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [shouldRefresh, filters]);

  const elem = document.querySelector('#deployed-task-dag-container');
  const DAGContainerSize = useSize(elem as any);

  // add debounced refresh effect on changing search name value
  useDebouncedUpdateEffect(() => {
    dispatch.scheduledTasks.setShouldRefresh(true);
  }, [filters.searchName]);

  // when other filters change, reload table data immediately.
  // Shall not trigger after mount
  useUpdateEffect(() => {
    dispatch.scheduledTasks.setShouldRefresh(true);
  }, [
    filters.taskTemplateName,
    filters.ownerIds,
    filters.pageNum,
    filters.pageSize,
  ]);

  useUnmount(() => {
    dispatch.scheduledTasks.resetAll();
  });

  return (
    <div id="scheduled-task-view">
      <Headings />
      <main
        id="scheduled-task-view-main-content"
        className={styles.MainContent}
      >
        {/* Table container */}
        <section className={styles.TableContainer}>
          <Card>
            <DeployedTasksTable
              selectedTask={selectedTask}
              setSelectedTask={setSelectedTask}
            />
          </Card>
        </section>
        {/* DAG container */}
        <section
          id="deployed-task-dag-container"
          className={styles.DAGContainer}
        >
          <DeployedTaskDAG
            task={selectedTask}
            width={DAGContainerSize.width}
            height={DAGContainerSize.height}
          />
        </section>
      </main>
    </div>
  );
};

export default ScheduledTaskView;
