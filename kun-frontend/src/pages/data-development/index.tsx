import React, { memo, useCallback } from 'react';
import {
  ReflexContainer,
  ReflexSplitter,
  ReflexElement,
} from 'react-reflex';
import { useEventEmitter, useUnmount, useUpdateEffect } from 'ahooks';
import useRedux from '@/hooks/useRedux';
import useDebouncedUpdateEffect from '@/hooks/useDebouncedUpdateEffect';
import { createTaskDefinition } from '@/services/data-development/task-definitions';

import { TaskDefinitionFilterToolbar } from '@/pages/data-development/components/TaskDefinitionFilterToolbar';
import { TaskDefinitionCreationModal } from '@/pages/data-development/components/TaskDefinitionCreationModal';
import { TasksGraphPanel } from '@/pages/data-development/sub-layout/TasksGraphPanel';
import { TaskViewsAside } from '@/pages/data-development/components/TaskViewsAside/TaskViewsAside';

import { TaskDefinition } from '@/definitions/TaskDefinition.type';
import { TaskTemplate } from '@/definitions/TaskTemplate.type';
import { DataDevelopmentModelFilter } from '@/rematch/models/dataDevelopment/model-state';

import { ViewContext } from './context';
import 'react-reflex/styles.css';
import styles from './index.less';

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
            <TaskViewsAside
              views={[]}
            />
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
