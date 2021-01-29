import React, { memo, useCallback, useEffect, useState } from 'react';
import { ReflexContainer, ReflexSplitter, ReflexElement } from 'react-reflex';
import { useUnmount } from 'ahooks';
import useRedux from '@/hooks/useRedux';
import useDebouncedUpdateEffect from '@/hooks/useDebouncedUpdateEffect';
import uniq from 'lodash/uniq';

import { TaskDefinitionFilterToolbar } from '@/pages/data-development/components/FilterToolbar/TaskDefinitionFilterToolbar';
import { TaskViewsAside } from '@/pages/data-development/components/TaskViewsAside/TaskViewsAside';
import { TaskDefinitionTable } from '@/pages/data-development/components/TaskDefinitionTable/TaskDefinitionTable';
import { TaskDefViewModificationModal } from '@/pages/data-development/components/TaskDefViewModificationModal/TaskDefViewModificationModal';
import { TaskTemplateCreateDropMenu } from '@/pages/data-development/components/TaskTemplateCreateDropMenu/TaskTemplateCreateDropMenu';
import { TaskDefToViewTransferModal } from '@/pages/data-development/components/TaskDefToViewTransfererModal/TaskDefToViewTransferModal';
import { AddToOtherViewModal } from '@/pages/data-development/components/AddToOtherViewModal/AddToOtherViewModal';
import { TaskDAGViewWrapper } from '@/pages/data-development/components/TaskDAG/TaskDAGViewWrapper';

import {
  createTaskDefinitionView,
  deleteTaskDefinitionView,
  overwriteIncludingTaskDefinitionsOfView,
  putTaskDefinitionsIntoView,
  removeTaskDefinitionsFromView,
  updateTaskDefinitionView,
} from '@/services/data-development/task-definition-views';
import { createTaskDefinition } from '@/services/data-development/task-definitions';

import {
  TaskDefinitionViewBase,
  TaskDefinitionViewUpdateVO,
  TaskDefinitionViewVO,
} from '@/definitions/TaskDefinitionView.type';
import { DataDevelopmentModelFilter } from '@/rematch/models/dataDevelopment/model-state';

import 'react-reflex/styles.css';
import { StringParam, useQueryParams } from 'use-query-params';
import { fetchDeployedTasks } from '@/services/task-deployments/deployed-tasks';
import { message } from 'antd';
import { createAndRunBackfill } from '@/services/data-backfill/backfill.services';
import useI18n from '@/hooks/useI18n';
import { ConfirmBackfillCreateModal } from '@/pages/data-development/components/ConfirmBackfillCreateModal/ConfirmBackfillCreateModal';
import styles from './index.less';

const DataDevelopmentPage: React.FC<any> = memo(function DataDevelopmentPage() {
  const {
    selector: {
      filters,
      displayType,
      taskDefViewsList,
      loadingViews,
      selectedView,
    },
    dispatch,
  } = useRedux<{
    filters: DataDevelopmentModelFilter;
    displayType: 'LIST' | 'DAG';
    taskDefViewsList: TaskDefinitionViewBase[];
    loadingViews: boolean;
    selectedView: TaskDefinitionViewBase | null;
    loadingTaskDefs: boolean;
  }>(s => ({
    filters: s.dataDevelopment.filters,
    displayType: s.dataDevelopment.displayType,
    taskDefViewsList: s.dataDevelopment.taskDefViewsList,
    loadingViews: s.loading.effects.dataDevelopment.fetchTaskDefViews,
    selectedView: s.dataDevelopment.selectedTaskDefView,
    loadingTaskDefs: s.loading.effects.dataDevelopment.fetchTaskDefinitions,
  }));

  const t = useI18n();

  const [query, setQuery] = useQueryParams({
    view: StringParam,
  });
  const [taskDefViewSearchKeyword, setTaskDefViewSearchKeyword] = useState<
    string
  >('');
  const [createViewModalVisible, setCreateViewModalVisible] = useState<boolean>(
    false,
  );
  const [editView, setEditView] = useState<TaskDefinitionViewVO | null>(null);
  const [transferModalVisible, setTransferModalVisible] = useState<boolean>(
    false,
  );
  const [addToOtherViewModalVisible, setAddToOtherViewModalVisible] = useState<
    boolean
  >(false);
  const [selectedTaskDefIds, setSelectedTaskDefIds] = useState<string[]>([]);

  const [updateTime, setUpdateTime] = useState<number>(Date.now());

  const [viewIdFromQueryInitialized, setViewIdFromQueryInitialized] = useState<
    boolean
  >(false);

  const [
    confirmBackfillCreateModalVisible,
    setConfirmBackfillCreateModalVisible,
  ] = useState<boolean>(false);

  useEffect(() => {
    if (
      !viewIdFromQueryInitialized &&
      taskDefViewsList &&
      taskDefViewsList.length &&
      query.view
    ) {
      const targetView = taskDefViewsList.find(view => view.id === query.view);
      if (targetView) {
        dispatch.dataDevelopment.setSelectedTaskDefinitionView(targetView);
      }
      setViewIdFromQueryInitialized(true);
    }
  }, [dispatch.dataDevelopment, query.view, taskDefViewsList, viewIdFromQueryInitialized]);

  useUnmount(() => {
    // reset state & free up memory
    dispatch.dataDevelopment.setCreatingTaskTemplate(null);
    setViewIdFromQueryInitialized(false);
  });

  /* Task definition view effects and callbacks */

  const searchTaskDefViews = useCallback(() => {
    dispatch.dataDevelopment.fetchTaskDefViews({
      keyword: taskDefViewSearchKeyword,
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [taskDefViewSearchKeyword]);

  const forceTableRefresh = useCallback(() => {
    setUpdateTime(Date.now());
  }, []);

  useEffect(() => {
    searchTaskDefViews();
    // eslint-disable-next-line
  }, []);

  useDebouncedUpdateEffect(
    () => {
      searchTaskDefViews();
    },
    [taskDefViewSearchKeyword],
    {
      wait: 1000,
    },
  );

  const handleCreateView = useCallback(
    async (updateVO: TaskDefinitionViewUpdateVO) => {
      try {
        await createTaskDefinitionView({
          name: updateVO.name,
          taskDefinitionIds: updateVO.taskDefinitionIds,
        });
        setCreateViewModalVisible(false);
      } finally {
        searchTaskDefViews();
      }
    },
    [searchTaskDefViews],
  );

  const handleUpdateView = useCallback(
    async (updateVO: TaskDefinitionViewUpdateVO) => {
      if (editView) {
        try {
          await updateTaskDefinitionView(editView.id, {
            name: updateVO.name,
            taskDefinitionIds: editView.includedTaskDefinitionIds || [],
          });
          setEditView(null);
        } finally {
          searchTaskDefViews();
        }
      }
    },
    [editView, searchTaskDefViews],
  );

  const handleDeleteView = useCallback(
    async (viewId: string) => {
      try {
        if (viewId === selectedView?.id) {
          dispatch.dataDevelopment.setSelectedTaskDefinitionView(null);
          setQuery({
            view: null,
          });
        }
        await deleteTaskDefinitionView(viewId);
        setEditView(null);
      } finally {
        searchTaskDefViews();
      }
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [selectedView?.id],
  );

  const handleTaskDefinitionCreate = useCallback(
    async (
      taskTemplateName: string,
      name: string,
      createInCurrentView: boolean,
    ) => {
      try {
        const createdTaskDefinition = await createTaskDefinition({
          name,
          taskTemplateName,
        });
        if (createInCurrentView && selectedView && createdTaskDefinition) {
          await putTaskDefinitionsIntoView(selectedView.id, [
            createdTaskDefinition.id as string,
          ]);
        }
      } finally {
        searchTaskDefViews();
        forceTableRefresh();
      }
    },
    [forceTableRefresh, searchTaskDefViews, selectedView],
  );

  const handleAddTaskDefsToOtherView = useCallback(
    async (targetViewId: string) => {
      try {
        await putTaskDefinitionsIntoView(targetViewId, selectedTaskDefIds);
      } catch (e) {
        // do nothing
      } finally {
        searchTaskDefViews();
        forceTableRefresh();
      }
    },
    [forceTableRefresh, searchTaskDefViews, selectedTaskDefIds],
  );

  const handleClickRunBackfill = useCallback(
    async (name: string, taskDefIds: string[]) => {
      try {
        const relatedDeployedTasks = await fetchDeployedTasks({
          definitionIds: taskDefIds,
        });
        const relatedWorkflowIds = uniq(
          (relatedDeployedTasks?.records || []).map(
            deployedTask => deployedTask.workflowTaskId,
          ),
        );
        if (relatedWorkflowIds.length !== taskDefIds.length) {
          message.error(
            t('dataDevelopment.runBackfillTaskDefNotPublishedMessage'),
          );
          return;
        }
        await createAndRunBackfill({
          name,
          workflowTaskIds: relatedWorkflowIds,
          taskDefinitionIds: taskDefIds,
        });
        message.success('Backfill created.');
      } catch (e) {
        message.error('Run backfill failed.');
      }
    },
    [t],
  );

  const handleRemoveTaskDefsFromCurrentView = useCallback(
    async (taskDefIdsToRemove: string[], viewId: string) => {
      try {
        if (taskDefIdsToRemove && taskDefIdsToRemove.length) {
          await removeTaskDefinitionsFromView(viewId, taskDefIdsToRemove);
        }
      } finally {
        searchTaskDefViews();
        forceTableRefresh();
        setSelectedTaskDefIds([]);
      }
    },
    [forceTableRefresh, searchTaskDefViews],
  );

  /* Task definition table renderer */
  const renderGraphOrTable = () => {
    if (displayType === 'LIST') {
      return (
        <div key="list-table" className={styles.tableComponentWrapper}>
          <TaskDefinitionTable
            taskDefViewId={selectedView?.id || null}
            filters={filters}
            updateTime={updateTime}
            onTransferToThisViewClick={() => {
              setTransferModalVisible(true);
            }}
            onAddToOtherViewBtnClick={() => {
              setAddToOtherViewModalVisible(true);
            }}
            selectedTaskDefIds={selectedTaskDefIds}
            setSelectedTaskDefIds={setSelectedTaskDefIds}
            onRemoveTaskDefsFromView={handleRemoveTaskDefsFromCurrentView}
            onClickRunBackfill={() => {
              setConfirmBackfillCreateModalVisible(true);
            }}
          />
        </div>
      );
    }
    // else
    return (
      <TaskDAGViewWrapper
        taskDefViewId={selectedView?.id || null}
        filters={filters}
        updateTime={updateTime}
        selectedTaskDefIds={selectedTaskDefIds}
        setSelectedTaskDefIds={setSelectedTaskDefIds}
        setAddToOtherViewModalVisible={setAddToOtherViewModalVisible}
      />
    );
  };

  return (
    <main className={styles.Page}>
      <TaskTemplateCreateDropMenu
        onCreateTaskDefinition={handleTaskDefinitionCreate}
      />
      {/* Layout */}
      <ReflexContainer orientation="vertical">
        {/* Task types select left aside */}
        <ReflexElement className={styles.leftPane} flex={0.192} minSize={200}>
          <TaskViewsAside
            loading={loadingViews}
            views={taskDefViewsList as any[]}
            onSearch={(searchText: string) => {
              setTaskDefViewSearchKeyword(searchText);
            }}
            onClickCreateBtn={() => {
              setCreateViewModalVisible(true);
            }}
            onSelectItem={viewItem => {
              dispatch.dataDevelopment.setSelectedTaskDefinitionView(viewItem);
              setQuery({
                view: viewItem?.id || '',
              });
            }}
            onEdit={view => {
              setEditView(view);
            }}
            selectedView={selectedView}
            updateTime={updateTime}
          />
        </ReflexElement>

        {/* Splitter */}
        <ReflexSplitter propagate />

        {/* Center task DAG graph / list table container */}
        <ReflexElement className={styles.mainPane} flex={0.847} minSize={800}>
          <TaskDefinitionFilterToolbar />
          {renderGraphOrTable()}
        </ReflexElement>
      </ReflexContainer>

      {/* Floating elements */}
      <TaskDefViewModificationModal
        mode="create"
        visible={createViewModalVisible}
        onOk={handleCreateView}
        onCancel={() => {
          setCreateViewModalVisible(false);
        }}
      />
      <TaskDefViewModificationModal
        mode="edit"
        visible={editView != null}
        initView={editView}
        onOk={handleUpdateView}
        onDelete={handleDeleteView}
        onCancel={() => {
          setEditView(null);
        }}
      />
      {/* Transfer tasks to current selected view */}
      <TaskDefToViewTransferModal
        viewsList={taskDefViewsList}
        onOk={async selectedTaskDefinitionIds => {
          try {
            if (selectedView) {
              await overwriteIncludingTaskDefinitionsOfView(
                selectedView.id,
                selectedTaskDefinitionIds,
              );
            }
            setTransferModalVisible(false);
          } finally {
            searchTaskDefViews();
            forceTableRefresh();
          }
        }}
        onCancel={() => {
          setTransferModalVisible(false);
        }}
        visible={transferModalVisible}
        lockTargetView
        initTargetView={transferModalVisible ? selectedView : null}
      />
      <AddToOtherViewModal
        visible={addToOtherViewModalVisible}
        taskDefViews={taskDefViewsList}
        currentViewId={selectedView?.id || null}
        onOk={targetViewId => {
          return handleAddTaskDefsToOtherView(targetViewId).then(() => {
            setAddToOtherViewModalVisible(false);
          });
        }}
        onCancel={() => {
          setAddToOtherViewModalVisible(false);
        }}
      />
      <ConfirmBackfillCreateModal
        visible={confirmBackfillCreateModalVisible}
        onConfirm={handleClickRunBackfill}
        selectedTaskDefIds={selectedTaskDefIds}
        onCancel={() => {
          setConfirmBackfillCreateModalVisible(false);
        }}
      />
    </main>
  );
});

export default DataDevelopmentPage;
