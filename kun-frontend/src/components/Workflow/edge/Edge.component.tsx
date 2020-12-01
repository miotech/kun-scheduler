import React, { memo } from 'react';
import { WorkflowEdge } from '@/components/Workflow/Workflow.typings';
import { buildDAGEdgePath } from '@/components/Workflow/helpers/buildDAGEdgePath';

interface OwnProps {
  edge: WorkflowEdge;
}

type Props = OwnProps;

export const TaskDAGEdge: React.FC<Props> = memo(function TaskDAGEdge(props) {
  const {
    edge,
  } = props;

  return (
    <g data-tid={`edge-${edge.srcNodeId}-${edge.destNodeId}-wrapper`}>
      <path
        stroke="#4869FC"
        strokeWidth={2}
        fill="transparent"
        markerEnd="url(#arrowEnd)"
        data-tid={`edge-${edge.srcNodeId}-${edge.destNodeId}`}
        d={buildDAGEdgePath(edge, {})}
      />
    </g>
  );
});
