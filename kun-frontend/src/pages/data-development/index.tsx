import React, { memo, useCallback, useEffect, useState } from 'react';
import {
  ReflexContainer,
  ReflexSplitter,
  ReflexElement,
} from 'react-reflex';
import { useMount, useUnmount, useUpdateEffect } from 'ahooks';
import useRedux from '@/hooks/useRedux';
import useDebouncedUpdateEffect from '@/hooks/useDebouncedUpdateEffect';
// import { createTaskDefinition } from '@/services/data-development/task-definitions';

import { TaskDefinitionFilterToolbar } from '@/pages/data-development/components/FilterToolbar/TaskDefinitionFilterToolbar';
// import { TaskDefinitionCreationModal } from '@/pages/data-development/components/TaskDefinitionCreationModal';
// import { TasksGraphPanel } from '@/pages/data-development/sub-layout/TasksGraphPanel';
import { TaskViewsAside } from '@/pages/data-development/components/TaskViewsAside/TaskViewsAside';

// import { TaskDefinition } from '@/definitions/TaskDefinition.type';
// import { TaskTemplate } from '@/definitions/TaskTemplate.type';
import { DataDevelopmentModelFilter } from '@/rematch/models/dataDevelopment/model-state';

import { TaskDefinitionTable } from '@/pages/data-development/components/TaskDefinitionTable/TaskDefinitionTable';
import { TaskDefViewModificationModal } from '@/pages/data-development/components/TaskDefViewModificationModal/TaskDefViewModificationModal';
import {
  TaskDefinitionViewBase, TaskDefinitionViewUpdateVO, TaskDefinitionViewVO
} from '@/definitions/TaskDefinitionView.type';

import 'react-reflex/styles.css';
import {
  createTaskDefinitionView, deleteTaskDefinitionView, updateTaskDefinitionView
} from '@/services/data-development/task-definition-views';
import { TaskTemplateCreateDropMenu } from '@/pages/data-development/components/TaskTemplateCreateDropMenu/TaskTemplateCreateDropMenu';
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
    filters: DataDevelopmentModelFilter,
    displayType: 'LIST' | 'DAG',
    taskDefViewsList: TaskDefinitionViewBase[],
    loadingViews: boolean,
    selectedView: TaskDefinitionViewBase | null,
    loadingTaskDefs: boolean,
  }>(s => ({
    filters: s.dataDevelopment.filters,
    displayType: s.dataDevelopment.displayType,
    taskDefViewsList: s.dataDevelopment.taskDefViewsList,
    loadingViews: s.loading.effects.dataDevelopment.fetchTaskDefViews,
    selectedView: s.dataDevelopment.selectedTaskDefView,
    loadingTaskDefs: s.loading.effects.dataDevelopment.fetchTaskDefinitions,
  }));

  const [ taskDefViewSearchKeyword, setTaskDefViewSearchKeyword ] = useState<string>('');
  const [ createViewModalVisible, setCreateViewModalVisible ] = useState<boolean>(false);
  const [ editView, setEditView ] = useState<TaskDefinitionViewVO | null>(null);

  useMount(() => {
  });

  useUnmount(() => {
    // reset state & free up memory
    dispatch.dataDevelopment.setCreatingTaskTemplate(null);
  });

  /*
  const handleCreationModalCancel = useCallback(() => {
    dispatch.dataDevelopment.setCreatingTaskTemplate(null);
  }, [
    dispatch,
  ]);

  const handleCreateTask = useCallback((taskName: string) => {
    if (creatingTaskTemplate) {
      createTaskDefinition({
        name: taskName,
        taskTemplateName: creatingTaskTemplate.name,
      }).then(response => {
        if (response) {
          // reset and close modal
          dispatch.dataDevelopment.setCreatingTaskTemplate(null);
          // refresh task defs
          dispatch.dataDevelopment.fetchTaskDefinitions();
          dispatch.dataDevelopment.fetchTaskDefinitionsForDAG();
        }
      });
    }
  }, [
    creatingTaskTemplate,
    dispatch,
  ]);
  */

  /* Task definition view effects and callbacks */

  const searchTaskDefViews = () => {
    dispatch.dataDevelopment.fetchTaskDefViews({
      keyword: taskDefViewSearchKeyword,
    });
  };

  useEffect(() => {
    searchTaskDefViews();
    // eslint-disable-next-line
  }, []);

  useDebouncedUpdateEffect(() => {
    searchTaskDefViews();
  }, [
    taskDefViewSearchKeyword,
  ], {
    wait: 1000,
  });

  const handleCreateView = useCallback(async (updateVO: TaskDefinitionViewUpdateVO) => {
    try {
      await createTaskDefinitionView({
        name: updateVO.name,
        taskDefinitionIds: updateVO.taskDefinitionIds,
      });
      setCreateViewModalVisible(false);
    } finally {
      searchTaskDefViews();
    }
  }, [
    searchTaskDefViews,
  ]);

  const handleUpdateView = useCallback(async (updateVO: TaskDefinitionViewUpdateVO) => {
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
  }, [
    editView,
    searchTaskDefViews,
  ]);

  const handleDeleteView = useCallback(async (viewId: string) => {
    try {
      await deleteTaskDefinitionView(viewId);
      setEditView(null);
    } finally {
      searchTaskDefViews();
    }
  }, []);

  /* Task definition table renderer */
  const renderGraphOrTable = () => {
    if (displayType === 'LIST') {
      return (
        <div key="list-table" className={styles.tableComponentWrapper}>
          <TaskDefinitionTable
            taskDefViewId={selectedView?.id || null}
            filters={filters}
          />
        </div>
      );
    }
      return null;
  };

  return (
    <main className={styles.Page}>
      <TaskTemplateCreateDropMenu />
      {/* Layout */}
      <ReflexContainer
        orientation="vertical"
      >
        {/* Task types select left aside */}
        <ReflexElement
          className={styles.leftPane}
          flex={0.192}
          minSize={200}
        >
          <TaskViewsAside
            loading={loadingViews}
            views={taskDefViewsList as any[]}
            onSearch={(searchText: string) => {
              setTaskDefViewSearchKeyword(searchText);
            }}
            onClickCreateBtn={() => {
              setCreateViewModalVisible(true);
            }}
            onSelectItem={(viewItem) => {
              dispatch.dataDevelopment.setSelectedTaskDefinitionView(viewItem);
            }}
            onEdit={(view) => {
              setEditView(view);
            }}
            selectedView={selectedView}
          />
        </ReflexElement>
        <ReflexSplitter propagate />
        {/* Center task graph */}
        <ReflexElement
          className={styles.mainPane}
          flex={0.847}
          minSize={800}
        >
          <TaskDefinitionFilterToolbar />
          {renderGraphOrTable()}
        </ReflexElement>
      </ReflexContainer>
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
    </main>
  );
});

export default DataDevelopmentPage;
