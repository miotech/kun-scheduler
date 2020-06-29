import React, { useContext, useMemo } from 'react';
import { TaskDefinition } from '@/definitions/TaskDefinition.type';
import { TaskDefinitionItem } from '@/pages/data-development/components/TaskDefinitionItem';
import { ViewContext } from '@/pages/data-development/context';

import styles from './TaskDefinitionList.less';

export interface TaskDefinitionListProps {
  items: TaskDefinition[];
  onEditTaskDef?: (taskDef: TaskDefinition) => any;
}

export const TaskDefinitionList: React.FC<TaskDefinitionListProps> = props => {

  const { viewportCenter$ } = useContext(ViewContext);

  const items = useMemo(() => (props.items || []).map(taskDef => (
    <TaskDefinitionItem
      key={taskDef.id}
      taskDefinition={taskDef}
      onClick={props.onEditTaskDef}
      onClickEdit={props.onEditTaskDef}
      onClickCenter={def => {
        viewportCenter$?.emit(def.id);
      }}
    />
  )), [
    props.onEditTaskDef,
    viewportCenter$,
    props.items,
  ]);

  return (
    <ul className={styles.TaskDefinitionList}>
      {items}
    </ul>
  );
};
