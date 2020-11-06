import React, { FunctionComponent, RefObject, useCallback, useRef, useState } from 'react';
import { useMount, useSize, useUnmount, useUpdateEffect } from 'ahooks';
import useRedux from '@/hooks/useRedux';
import { useRouteMatch } from 'umi';
import { ReflexContainer, ReflexElement, ReflexSplitter } from 'react-reflex';

import TaskRunsFilterBar from '@/pages/operation-center/deployed-task-detail/components/TaskRunsFilterBar';
import TaskRunsTable from '@/pages/operation-center/deployed-task-detail/components/TaskRunsTable';
import { KunSpin } from '@/components/KunSpin';
import { RightPanel } from '@/pages/operation-center/scheduled-tasks/components/RightPanel';
import { TaskRun } from '@/definitions/TaskRun.type';

import 'react-reflex/styles.css';
import styles from './index.less';

interface DeployedTaskDetailViewProps {}

const DeployedTaskDetailView: FunctionComponent<DeployedTaskDetailViewProps> = () => {
  const match = useRouteMatch<{ id: string }>();

  const rightPanelRef = useRef() as RefObject<any>;

  const { selector: {
    filters,
    taskRunsData,
    taskRunsCount,
    taskDetailIsLoading,
    taskRunsIsLoading,
  }, dispatch } = useRedux(s => ({
    filters: s.deployedTaskDetail.filters,
    taskRunsData: s.deployedTaskDetail.taskRuns,
    taskRunsCount: s.deployedTaskDetail.taskRunsCount,
    taskDetailIsLoading: s.loading.effects.deployedTaskDetail.loadDeployedTaskDetailById,
    taskRunsIsLoading: s.loading.effects.deployedTaskDetail.loadTaskRuns,
  }));

  const [ selectedTaskRun, setSelectedTaskRun ] = useState<TaskRun | null>(null);

  useMount(() => {
    // highlight corresponding aside menu item
    dispatch.route.updateCurrentPath('/operation-center/scheduled-tasks/:id');
    dispatch.route.updateCurrentParams({
      id: match.params.id,
    });
    dispatch.deployedTaskDetail.setDeployedTaskId(match.params.id);
    dispatch.deployedTaskDetail.loadDeployedTaskDetailById(match.params.id);
    dispatch.deployedTaskDetail.loadTaskRuns({
      id: match.params.id,
      ...filters,
    });
  });

  useUnmount(() => {
    // clear state after unmount
    dispatch.deployedTaskDetail.resetAll();
  });

  useUpdateEffect(() => {
    dispatch.deployedTaskDetail.loadTaskRuns({
      id: match.params.id,
      ...filters,
    });
  }, [
    match.params.id,
    filters.status,
    filters.pageNum,
    filters.pageSize,
    filters.startTime,
    filters.endTime,
  ]);

  const handleClickRefreshBtn = useCallback(() => {
    dispatch.deployedTaskDetail.loadTaskRuns({
      id: match.params.id,
      ...filters,
    });
  }, [
    dispatch,
    match.params.id,
    filters,
  ]);

  const handleChangePagination = useCallback((nextPageNum: number, nextPageSize?: number) => {
    dispatch.deployedTaskDetail.updateFilter({
      pageNum: nextPageNum,
      pageSize: nextPageSize,
    });
  }, [dispatch]);

  const dagContainerSize = useSize(rightPanelRef.current);

  if (taskDetailIsLoading) {
    return (
      <KunSpin spinning>
        <main id="deployed-task-detail-view" className={styles.View} />
      </KunSpin>
    );
  }

  return (
    <main id="deployed-task-detail-view" className={styles.View}>
      <TaskRunsFilterBar
        filter={filters}
        dispatch={dispatch}
        onClickRefresh={handleClickRefreshBtn}
        taskDefId={match.params.id}
      />
      <ReflexContainer
        // id="deployed-task-detail-main-content"
        className={styles.ContentContainer}
        orientation="vertical"
      >
        <ReflexElement
          className={styles.LeftPanel}
          flex={5}
          minSize={192}
        >
          <KunSpin spinning={taskRunsIsLoading}>
            <TaskRunsTable
              tableData={taskRunsData || []}
              pageNum={filters.pageNum}
              pageSize={filters.pageSize}
              total={taskRunsCount}
              onChangePagination={handleChangePagination}
              selectedTaskRun={selectedTaskRun}
              setSelectedTaskRun={setSelectedTaskRun}
            />
          </KunSpin>
        </ReflexElement>
        <ReflexSplitter propagate />
        <ReflexElement
          className={styles.RightPanel}
          flex={5}
          minSize={192}
        >
          <RightPanel
            rightPanelRef={rightPanelRef}
            selectedTaskRun={selectedTaskRun}
            dagContainerSize={dagContainerSize}
          />
        </ReflexElement>
      </ReflexContainer>
    </main>
  );
};

export default DeployedTaskDetailView;
