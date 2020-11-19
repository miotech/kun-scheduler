import React, { memo } from 'react';
import { WorkflowEdge } from '@/components/Workflow/Workflow.typings';
import { buildLineageEdgePath } from '@/components/LineageDiagram/LineageBoard/helpers/buildLineageEdgePath';

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
        stroke="#d8d8d8"
        strokeWidth={2}
        fill="transparent"
        markerEnd="url(#arrowEnd)"
        data-tid={`edge-${edge.srcNodeId}-${edge.destNodeId}`}
        d={buildLineageEdgePath({ x: edge.srcX, y: edge.srcY }, { x: edge.destX, y: edge.destY })}
      />
    </g>
  );
});
