import React, { memo, useMemo } from 'react';
import { TaskDAGEdge } from '@/components/Workflow/edge/Edge.component';

import { WorkflowEdge, WorkflowNode } from '@/components/Workflow/Workflow.typings';

interface OwnProps {
  nodes: WorkflowNode[];
  edges: WorkflowEdge[];
}

type Props = OwnProps;

export const EdgeRenderer: React.FC<Props> = memo(function EdgeRenderer(props) {
  const {
    nodes,
    edges,
  } = props;

  const nodeIdSet = useMemo(() => {
    const s: Set<string> = new Set();
    nodes.forEach(n => { s.add(n.id); });
    return s;
  }, [nodes]);

  return <>
    {
      edges.map(e => {
        const { srcNodeId, destNodeId } = e;
        if (nodeIdSet.has(srcNodeId) && nodeIdSet.has(destNodeId)) {
          return <TaskDAGEdge key={`${srcNodeId}-${destNodeId}`} edge={e} />;
        }
        // else
        return null;
      })
    }
  </>;
});
