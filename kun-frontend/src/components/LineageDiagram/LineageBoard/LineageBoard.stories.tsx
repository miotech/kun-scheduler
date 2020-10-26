import React, { useEffect, useState } from 'react';
import { Meta } from '@storybook/react';
import { IntlProvider } from '@@/core/umiExports';

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
  return (
    <IntlProvider locale="en-US" messages={demoMessage}>
      <div style={{ width: '100%', height: '600px' }}>
        <LineageBoard
          rankdir="LR"
          nodes={initialNodes}
          edges={initialEdges}
          ranker="tight-tree"
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
