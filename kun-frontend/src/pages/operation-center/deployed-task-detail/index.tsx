import React, { FunctionComponent, RefObject, useCallback, useEffect, useRef, useState } from 'react';
import { useMount, useSize, useUnmount, useUpdateEffect } from 'ahooks';
import { useRouteMatch } from 'umi';
import { ReflexContainer, ReflexElement, ReflexSplitter } from 'react-reflex';
import { StringParam, useQueryParams } from 'use-query-params';

import useRedux from '@/hooks/useRedux';
import TaskRunsFilterBar from '@/pages/operation-center/deployed-task-detail/components/TaskRunsFilterBar';
import TaskRunsTable from '@/pages/operation-center/deployed-task-detail/components/TaskRunsTable';
import { KunSpin } from '@/components/KunSpin';
import { RightPanel } from '@/pages/operation-center/scheduled-tasks/components/RightPanel';
import { abortTaskRunInstance, restartTaskRunInstance } from '@/services/task-deployments/deployed-tasks';

import { TaskRun } from '@/definitions/TaskRun.type';

import 'react-reflex/styles.css';
import styles from './index.less';

interface DeployedTaskDetailViewProps {}

const DeployedTaskDetailView: FunctionComponent<DeployedTaskDetailViewProps> = () => {
  const match = useRouteMatch<{ id: string }>();

  const rightPanelRef = useRef() as RefObject<any>;

  const {
    selector: { filters, taskRunsData, taskRunsCount, taskDetailIsLoading, taskRunsIsLoading },
    dispatch,
  } = useRedux(s => ({
    filters: s.deployedTaskDetail.filters,
    taskRunsData: s.deployedTaskDetail.taskRuns,
    taskRunsCount: s.deployedTaskDetail.taskRunsCount,
    taskDetailIsLoading: s.loading.effects.deployedTaskDetail.loadDeployedTaskDetailById,
    taskRunsIsLoading: s.loading.effects.deployedTaskDetail.loadTaskRuns,
  }));

  const [selectedTaskRun, setSelectedTaskRun] = useState<TaskRun | null>(null);

  const [query, setQuery] = useQueryParams({
    taskRunId: StringParam,
  });

  useMount(async () => {
    // highlight corresponding aside menu item
    dispatch.route.updateCurrentPath('/operation-center/scheduled-tasks/:id');
    dispatch.route.updateCurrentParams({
      id: match.params.id,
    });
    dispatch.deployedTaskDetail.setDeployedTaskId(match.params.id);
    await dispatch.deployedTaskDetail.loadDeployedTaskDetailById(match.params.id);
    await dispatch.deployedTaskDetail.loadTaskRuns({
      id: match.params.id,
      ...filters,
    });
  });

  const [selectedTaskInitialized, setSelectedTaskInitialized] = useState<boolean>(false);

  useMount(() => {
    // highlight corresponding aside menu item
    dispatch.route.updateCurrentPath('/operation-center/scheduled-tasks/:id');
  });

  useEffect(() => {
    if (query.taskRunId && query.taskRunId.length && !selectedTaskInitialized) {
      const matchedTaskRunRecord = (taskRunsData || []).find(record => record.id === query.taskRunId);
      if (matchedTaskRunRecord != null) {
        setSelectedTaskRun(matchedTaskRunRecord);
        setSelectedTaskInitialized(true);
      }
    }
  }, [query.taskRunId, selectedTaskInitialized, taskRunsData]);

  useUpdateEffect(() => {
    if (selectedTaskRun?.id) {
      setQuery({
        taskRunId: selectedTaskRun.id,
      });
    }
  }, [selectedTaskRun?.id]);

  useUnmount(() => {
    // clear state after unmount
    setSelectedTaskInitialized(false);
    dispatch.deployedTaskDetail.resetAll();
  });

  useUpdateEffect(() => {
    setSelectedTaskInitialized(false);
    dispatch.deployedTaskDetail
      .loadTaskRuns({
        id: match.params.id,
        ...filters,
      })
      .finally(() => {
        setSelectedTaskInitialized(true);
      });
  }, [match.params.id, filters.status, filters.pageNum, filters.pageSize, filters.startTime, filters.endTime]);

  const doRefresh = useCallback(() => {
    dispatch.deployedTaskDetail.loadTaskRuns({
      id: match.params.id,
      ...filters,
    });
  }, [dispatch, match.params.id, filters]);

  const handleChangePagination = useCallback(
    (nextPageNum: number, nextPageSize?: number) => {
      dispatch.deployedTaskDetail.updateFilter({
        pageNum: nextPageNum,
        pageSize: nextPageSize,
      });
    },
    [dispatch],
  );

  const handleRestartTaskRun = useCallback(
    async function handleRestartTaskRun(taskRun: TaskRun | null) {
      if (taskRun) {
        try {
          await restartTaskRunInstance(taskRun.id);
        } finally {
          doRefresh();
        }
      }
    },
    [doRefresh],
  );

  const handleAbortTaskRun = useCallback(
    async function handleAbortTaskRun(taskRun: TaskRun | null) {
      if (taskRun) {
        try {
          await abortTaskRunInstance(taskRun.id);
        } finally {
          doRefresh();
        }
      }
    },
    [doRefresh],
  );

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
      <TaskRunsFilterBar filter={filters} dispatch={dispatch} onClickRefresh={doRefresh} taskDefId={match.params.id} />
      <ReflexContainer
        // id="deployed-task-detail-main-content"
        className={styles.ContentContainer}
        orientation="vertical"
      >
        <ReflexElement className={styles.LeftPanel} flex={5} minSize={192}>
          <KunSpin spinning={taskRunsIsLoading}>
            <TaskRunsTable
              tableData={taskRunsData || []}
              pageNum={filters.pageNum}
              pageSize={filters.pageSize}
              total={taskRunsCount}
              onChangePagination={handleChangePagination}
              selectedTaskRun={selectedTaskRun}
              setSelectedTaskRun={setSelectedTaskRun}
              onClickRerunTaskRun={handleRestartTaskRun}
              onClickStopTaskRun={handleAbortTaskRun}
            />
          </KunSpin>
        </ReflexElement>
        <ReflexSplitter propagate />
        <ReflexElement className={styles.RightPanel} flex={5} minSize={192}>
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
