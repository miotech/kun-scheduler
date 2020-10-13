import React, { memo, useCallback } from 'react';
import {
  ReflexContainer,
  ReflexSplitter,
  ReflexElement,
} from 'react-reflex';
import { useEventEmitter, useUnmount, useUpdateEffect } from 'ahooks';
import useRedux from '@/hooks/useRedux';
import { TaskTemplate } from '@/definitions/TaskTemplate.type';
import { createTaskDefinition } from '@/services/data-development/task-definitions';

import { TaskDefinitionCreationModal } from '@/pages/data-development/components/TaskDefinitionCreationModal';
import { TaskTypeListPanel } from '@/pages/data-development/sub-layout/TaskTypeListPanel';
import { TasksGraphPanel } from '@/pages/data-development/sub-layout/TasksGraphPanel';
import { TaskDefinitionListPanel } from '@/pages/data-development/sub-layout/TaskDefinitionListPanel';

import 'react-reflex/styles.css';
import { TaskDefinition } from '@/definitions/TaskDefinition.type';

import { DataDevelopmentModelFilter } from '@/rematch/models/dataDevelopment/model-state';
import { TaskDefinitionFilterToolbar } from '@/pages/data-development/components/TaskDefinitionFilterToolbar';
import useDebouncedUpdateEffect from '@/hooks/useDebouncedUpdateEffect';
import styles from './index.less';
import { ViewContext } from './context';

const DataDevelopmentPage: React.FC<any> = memo(function DataDevelopmentPage() {

  const viewportCenter$ = useEventEmitter<string | number>();

  const {
    selector: {
      createModalVisible,
      creatingTaskTemplate,
      filters,
    },
    dispatch,
  } = useRedux<{
    createModalVisible: boolean;
    creatingTaskTemplate: TaskTemplate | null;
    editingTaskDefinition: TaskDefinition | null;
    filters: DataDevelopmentModelFilter,
  }>(s => ({
    createModalVisible: (!!s.dataDevelopment.creatingTaskTemplate),
    creatingTaskTemplate: s.dataDevelopment.creatingTaskTemplate,
    editingTaskDefinition: s.dataDevelopment.editingTaskDefinition,
    filters: s.dataDevelopment.filters,
  }));

  useUnmount(() => {
    // reset state & free up memory
    dispatch.dataDevelopment.setCreatingTaskTemplate(null);
    dispatch.dataDevelopment.setTaskDefinitions([]);
    dispatch.dataDevelopment.setEditingTaskDefinition(null);
  });

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

  useUpdateEffect(() => {
    dispatch.dataDevelopment.fetchTaskDefinitions(filters);
  }, [
    filters.pageNum,
    filters.pageSize,
    filters.taskTemplateName,
    filters.creatorIds,
  ]);

  useDebouncedUpdateEffect(() => {
    dispatch.dataDevelopment.fetchTaskDefinitions(filters);
  }, [
    filters.name,
  ]);

  return (
    <ViewContext.Provider value={{
      viewportCenter$,
    }}>
      <main className={styles.Page}>
        {/* Layout */}
        <ReflexContainer
          orientation="vertical"
        >
          {/* Task types select left aside */}
          <ReflexElement
            className={styles.leftPane}
            flex={0.192}
            minSize={192}
          >
            <aside className={styles.paneContentContainer}>
              <TaskTypeListPanel />
            </aside>
          </ReflexElement>
          <ReflexSplitter propagate />

          {/* Center task graph */}
          <ReflexElement
            className={styles.midPane}
            flex={0.847}
          >
            <div className={styles.paneContentContainer}>
              <TaskDefinitionFilterToolbar />
              <TasksGraphPanel />
            </div>
          </ReflexElement>
          <ReflexSplitter propagate />

          {/* Tasks list right aside */}
          <ReflexElement
            className={styles.rightPane}
            flex={0.320}
            minSize={320}
          >
            <aside className={styles.paneContentContainer}>
              <TaskDefinitionListPanel />
            </aside>
          </ReflexElement>
        </ReflexContainer>
        <TaskDefinitionCreationModal
          visible={createModalVisible}
          onOk={handleCreateTask}
          onCancel={handleCreationModalCancel}
          taskTemplateName={creatingTaskTemplate?.name}
        />
      </main>
    </ViewContext.Provider>
  );
});

export default DataDevelopmentPage;
