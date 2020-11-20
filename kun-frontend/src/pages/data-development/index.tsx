import React, { memo } from 'react';
import {
  ReflexContainer,
  ReflexSplitter,
  ReflexElement,
} from 'react-reflex';
import { useUnmount, useUpdateEffect } from 'ahooks';
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

import 'react-reflex/styles.css';
import { TaskDefinitionTable } from '@/pages/data-development/components/TaskDefinitionTable/TaskDefinitionTable';
import styles from './index.less';

const DataDevelopmentPage: React.FC<any> = memo(function DataDevelopmentPage() {
  const {
    selector: {
      filters,
      displayType,
    },
    dispatch,
  } = useRedux<{
    filters: DataDevelopmentModelFilter,
    displayType: 'LIST' | 'DAG',
  }>(s => ({
    filters: s.dataDevelopment.filters,
    displayType: s.dataDevelopment.displayType,
  }));

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

  const renderGraphOrTable = () => {
    if (displayType === 'LIST') {
      return (
        <div className={styles.tableComponentWrapper}>
          <TaskDefinitionTable
            taskDefViewId={null}
          />
        </div>
      );
    }
      return null;
  };

  return (
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
          className={styles.mainPane}
          flex={0.847}
        >
          <TaskDefinitionFilterToolbar />
          {renderGraphOrTable()}
        </ReflexElement>
      </ReflexContainer>
    </main>
  );
});

export default DataDevelopmentPage;
