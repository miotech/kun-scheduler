import React, { useContext, useMemo } from 'react';
import { DAGTaskGraph } from '@/components/DAGGraph';
import { TaskDefinition } from '@/definitions/TaskDefinition.type';
import { taskDefsToGraph } from '@/utils/TaskDefsToGraph';
import { ViewContext } from '@/pages/data-development/context';

export interface TaskDefsDAGProps {
  taskDefinitions: TaskDefinition[];
  width?: number;
  height?: number;
  showMiniMap?: boolean;
}

export const TaskDefsDAG: React.FC<TaskDefsDAGProps> = props => {
  const {
    taskDefinitions,
    ...restProps
  } = props;

  const { viewportCenter$ } = useContext(ViewContext);

  const { nodes, relations } = useMemo(() => {
    return taskDefsToGraph(taskDefinitions || []);
  }, [
    taskDefinitions,
  ]);

  return (
    <DAGTaskGraph
      nodes={nodes}
      relations={relations}
      viewportCenter$={viewportCenter$ || undefined}
      {...restProps}
    />
  );
};
