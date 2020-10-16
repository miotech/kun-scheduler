import React, { useCallback, useEffect, useState } from 'react';
import { Meta } from '@storybook/react';
import { IntlProvider } from '@@/core/umiExports';
import findIndex from 'lodash/findIndex';
import cloneDeep from 'lodash/cloneDeep';

import { LineageBoard } from '@/components/LineageDiagram/LineageBoard/LineageBoard';
import { initialNodes, initialEdges } from '@/components/LineageDiagram/LineageBoard/mock-data/mock-data';
import { LineageEdge, LineageNode } from '@/definitions/Lineage.type';

export default {
  title: 'components/LineageDiagram/LineageBoard',
  component: LineageBoard,
} as Meta;

const demoMessage = {
  'lineage.rowCount': 'Row Count',
  'lineage.dataSourceName': 'Data Source Name',
  'lineage.dataSourceType': 'Data Source Type',
};

export const LineageBoardDemo = () => {
  const [nodes, setNodes] = useState<LineageNode[]>(initialNodes);
  const [edges, setEdges ] = useState<LineageEdge[]>(initialEdges);

  const clearAllSelections = () => {
    const nextNodesState = cloneDeep(nodes).map(n => ({
      ...n,
      data: { ...n.data, selected: false },
    }));
    const nextEdgesState = cloneDeep(edges).map(e => ({
      ...e,
      selected: false,
    }));
    setNodes(() => nextNodesState);
    setEdges(() => nextEdgesState);
  };

  const handleNodeClickSelection = useCallback((node) => {
    const targetNodeIdx = findIndex(nodes, (n) => {
      return n.id === node.id;
    });
    if (targetNodeIdx >= 0) {
      const nextNodesState = cloneDeep(nodes)
        .map(n => ({ ...n, data: { ...n.data, selected: false } }));
      nextNodesState[targetNodeIdx] = {
        ...nextNodesState[targetNodeIdx],
        data: {
          ...nextNodesState[targetNodeIdx].data,
          selected: true,
        },
      };
      setNodes(nextNodesState);
    }
  }, [nodes]);

  const handleEdgeClickSelection = useCallback((edge: { destNodeId: string; srcNodeId: string }) => {
    const targetEdgeIdx = findIndex(edges, (e) => {
      return (e.src === edge.srcNodeId) && (e.dest === edge.destNodeId);
    });
    if (targetEdgeIdx >= 0) {
      const nextEdgesState = cloneDeep(edges)
        .map(e => ({ ...e, selected: false }));
      nextEdgesState[targetEdgeIdx] = {
        ...nextEdgesState[targetEdgeIdx],
        selected: true,
      };
      setEdges(nextEdgesState);
    }
  }, [edges]);

  return (
    <IntlProvider locale="en-US" messages={demoMessage}>
      <div style={{ width: '100%', height: '600px' }}>
        <LineageBoard
          rankdir="LR"
          nodes={nodes}
          edges={edges}
          ranker="tight-tree"
          onClickNode={(node) => {
            console.log('clicked node: ', node);
            clearAllSelections();
            handleNodeClickSelection(node);
          }}
          onClickEdge={(edge) => {
            console.log('clicked edge: ', edge);
            clearAllSelections();
            handleEdgeClickSelection(edge);
          }}
          onClickBackground={() => {
            console.log('clicked background');
            clearAllSelections();
          }}
        />
      </div>
    </IntlProvider>
  );
};

export const AnimatedBoardExample = () => {
  const [ loading, setLoading ] = useState<boolean>(true);
  const [ nodes, setNodes ] = useState<LineageNode[]>([]);
  const [ edges, setEdges ] = useState<LineageEdge[]>([]);

  useEffect(() => {
    setTimeout(() => {
      setLoading(false);
      setNodes(initialNodes);
      setEdges(initialEdges);
    }, 2000);
  }, []);

  return (
    <IntlProvider locale="en-US" messages={demoMessage}>
      <div style={{ width: '100%', height: '600px' }}>
        <LineageBoard
          loading={loading}
          rankdir="LR"
          nodes={nodes}
          edges={edges}
          ranker="tight-tree"
        />
      </div>
    </IntlProvider>
  );
};
