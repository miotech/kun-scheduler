import React from 'react';
import { Meta } from '@storybook/react';
import { IntlProvider } from '@@/core/umiExports';

import { LineageBoard } from '@/components/LineageDiagram/LineageBoard/LineageBoard';
import { LineageEdge, LineageNode } from '@/definitions/Lineage.type';

export default {
  title: 'components/LineageDiagram/LineageBoard',
  component: LineageBoard,
} as Meta;

const demoMessage = {
  'lineageDiagram.nodeCard.rowCount': 'Row Count'
};

const nodes: LineageNode[] = [
  {
    id: '81704446437888000',
    data: {
      id: '81704446437888000',
      name: 'dataset-1',
      database: 'mysql',
      datasource: 'foo',
      description: 'dataset-1-description',
      owners: ['admin'],
      glossaries: [],
      tags: [],
      type: 'mysql',
      schema: '',
      high_watermark: {
        user: '',
        time: 0,
      },
    },
  },
  {
    id: '81704446437888001',
    data: {
      id: '81704446437888001',
      name: 'dataset-2',
      database: 'mysql',
      datasource: 'foo',
      description: 'dataset-2-description',
      owners: ['admin'],
      glossaries: [],
      tags: [],
      type: 'mysql',
      schema: '',
      high_watermark: {
        user: '',
        time: 0,
      },
    },
  },
  {
    id: '81704446437888002',
    data: {
      id: '81704446437888002',
      name: 'dataset-3',
      database: 'postgres',
      datasource: 'foo',
      description: 'dataset-3-description',
      owners: ['admin'],
      glossaries: [],
      tags: [],
      type: 'postgres',
      schema: 'public',
      high_watermark: {
        user: '',
        time: 0,
      },
    },
  },
  {
    id: '81704446437888003',
    data: {
      id: '81704446437888003',
      name: 'dataset-4',
      database: 'hive',
      datasource: 'bar',
      description: 'dataset-4-description',
      owners: ['admin'],
      glossaries: [],
      tags: [],
      type: 'hive',
      schema: 'dm',
      high_watermark: {
        user: '',
        time: 0,
      },
    },
  },
  {
    id: '81704446437888004',
    data: {
      id: '81704446437888004',
      name: 'dataset-5',
      database: 'hive',
      datasource: 'bar',
      description: 'dataset-5-description',
      owners: ['admin'],
      glossaries: [],
      tags: [],
      type: 'hive',
      schema: 'dm',
      high_watermark: {
        user: '',
        time: 0,
      },
    },
  },
];

const edges: LineageEdge[] = [
  { from: '81704446437888000', to: '81704446437888002' },
  { from: '81704446437888001', to: '81704446437888002' },
  { from: '81704446437888002', to: '81704446437888003' },
  { from: '81704446437888002', to: '81704446437888004' },
];

export const LineageBoardDemo = () => {
  return (
    <IntlProvider locale="en-US" messages={demoMessage}>
      <div style={{ width: '100%', height: '600px' }}>
        <LineageBoard
          rankdir="RL"
          nodes={nodes}
          edges={edges}
        />
      </div>
    </IntlProvider>
  );
};
