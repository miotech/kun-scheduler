import React, { memo, useCallback } from 'react';
import { useMount } from 'ahooks';
import useI18n from '@/hooks/useI18n';
import useRedux from '@/hooks/useRedux';
import { history } from 'umi';
import SafeUrlAssembler from 'safe-url-assembler';

import { KunSpin } from '@/components/KunSpin';
import { TaskDefinitionList } from '@/pages/data-development/components/TaskDefinitionList';
import { TaskDefinitionListPagination } from '@/pages/data-development/components/TaskDefinitionListPagination';
import { TaskDefinition } from '@/definitions/TaskDefinition.type';

import styles from './TaskDefinitionListPanel.less';

export interface TaskDefinitionListPanelProps {
}

export const TaskDefinitionListPanel: React.FC<TaskDefinitionListPanelProps> = memo(() => {
  const t = useI18n();

  /* select states from rematch store */
  const {
    selector: {
      taskDefinitions,
      isLoading,
      pageNum,
      pageSize,
      totalCount,
    },
    dispatch,
  } = useRedux((s) => ({
    taskDefinitions: s.dataDevelopment.taskDefinitions,
    isLoading: s.loading.effects.dataDevelopment.fetchTaskDefinitions,
    pageNum: s.dataDevelopment.filters.pageNum as number,
    pageSize: s.dataDevelopment.filters.pageSize,
    totalCount: s.dataDevelopment.totalCount,
  }));

  // on component did mount
  useMount(() => {
    dispatch.dataDevelopment.fetchTaskDefinitions();
  });

  const handlePaginationChange = useCallback((nextPage: number, nextPageSize?: number) => {
    dispatch.dataDevelopment.updateFilter({
      pageNum: nextPage,
      pageSize: nextPageSize,
    });
  }, [
    pageNum,
    pageSize,
    dispatch,
  ]);

  return (
    <aside className={styles.TaskDefinitionListPanel}>
      <h2 className={styles.ListHeading} >
        {t('dataDevelopment.taskList')}
      </h2>
      {/* Task definition list */}
      <section
        className={styles.ListBody}
        data-tid="task-def-list"
      >
        <KunSpin spinning={isLoading} asBlock={!taskDefinitions.length}>
          <TaskDefinitionList
            items={taskDefinitions || []}
            onEditTaskDef={(taskDef: TaskDefinition) => {
              dispatch.dataDevelopment.setBackUrl(window.location.pathname + window.location.search);
              history.push(SafeUrlAssembler()
                .template('/data-development/task-definition/:taskDefId')
                .param({
                  taskDefId: taskDef.id,
                })
                .toString());
            }}
          />
        </KunSpin>
      </section>
      {/* List pagination footer */}
      <footer
        className={styles.Footer}
        data-tid="pagination-footer"
      >
        <TaskDefinitionListPagination
          current={pageNum}
          pageSize={pageSize}
          total={totalCount}
          onChange={handlePaginationChange}
        />
      </footer>
    </aside>
  );
});
