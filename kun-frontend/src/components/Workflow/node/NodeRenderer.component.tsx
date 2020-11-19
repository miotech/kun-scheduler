import React, { memo } from 'react';
import { WorkflowNode } from '@/components/Workflow/Workflow.typings';
import { TaskDAGNode } from '@/components/Workflow/node/TaskDAGNode.component';
import { TASK_DAG_NODE_HEIGHT, TASK_DAG_NODE_WIDTH } from '@/components/Workflow/Workflow.constants';

interface OwnProps {
  nodes: WorkflowNode[];
}

type Props = OwnProps;

export const NodeRenderer: React.FC<Props> = memo(function NodeRenderer(props) {
  const {
    nodes = [],
  } = props;

  if (!nodes.length) {
    return <></>;
  }

  return <>
    {nodes.map((node) => {
      return (
        <g
          id={`task-def-node-${node.id}`}
          key={`node-${node.id}`}
        >
          <foreignObject
            x={node.x}
            y={node.y}
            width={TASK_DAG_NODE_WIDTH}
            height={TASK_DAG_NODE_HEIGHT}
          >
            <TaskDAGNode
              taskDefinition={{
                id: node.id,
                name: node.name,
                taskTemplateName: node.taskTemplateName,
                isDeployed: node.isDeployed,
              }}
              selected={node.status === 'selected'}
            />
          </foreignObject>
        </g>
      );
    })}
  </>;
});
