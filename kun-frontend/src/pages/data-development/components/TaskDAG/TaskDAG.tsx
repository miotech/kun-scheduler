import React, { memo, useEffect, useRef, useState } from 'react';
// import useRedux from '@/hooks/useRedux';
import { WorkflowCanvas } from '@/components/Workflow/canvas/Canvas.component';
import { useSize } from 'ahooks';
import LogUtils from '@/utils/logUtils';
import { convertTaskDefinitionsToGraph } from '@/pages/data-development/helpers/taskDefsToGraph';

import { TaskDefinition } from '@/definitions/TaskDefinition.type';
import { WorkflowNode, WorkflowEdge } from '@/components/Workflow/Workflow.typings';

import styles from './TaskDAG.module.less';

interface OwnProps {
  taskDefinitions: TaskDefinition[];
}

type Props = OwnProps;

export const logger = LogUtils.getLoggers('TaskDAG');

export const TaskDAG: React.FC<Props> = memo(function TaskDAG(props) {
  const {
    taskDefinitions = [],
  } = props;
  const [ nodes, setNodes ] = useState<WorkflowNode[]>([]);
  const [ edges, setEdges ] = useState<WorkflowEdge[]>([]);

  useEffect(() => {
    if (taskDefinitions && taskDefinitions.length) {
      const {
        nodes: computedNodes,
        edges: computedEdges,
      } = convertTaskDefinitionsToGraph(taskDefinitions);
      setNodes(computedNodes);
      setEdges(computedEdges);
    }
  }, [
    taskDefinitions,
  ]);

  const containerRef = useRef<any>();
  const { width, height } = useSize(containerRef);

  return (
    <div
      ref={containerRef}
      className={styles.TaskDAGContainer}
      data-tid="task-dag-container"
    >
      <WorkflowCanvas
        width={(width || 3) - 2}
        height={(height || 3) - 2}
        nodes={nodes}
        edges={edges}
      />
    </div>
  );
});
