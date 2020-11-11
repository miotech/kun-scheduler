import React from 'react';
import { Meta } from '@storybook/react';

import { TaskDAGNode } from '@/components/TaskDAGGraph/TaskDAGNode';
import { IntlProvider } from '@@/core/umiExports';
import { TaskDefinition } from '@/definitions/TaskDefinition.type';

import '@/global.less';

export default {
  title: 'components/TaskDAG/TaskDAGNode',
  component: TaskDAGNode,
} as Meta;

const demoMessage = {};

const sampleTaskDefinition: TaskDefinition = {
  id: '112114706067492864',
  name: '从mongo导入: dm.just_a_random_test_worker',
  creator: '106929110202187776',
  isArchived: false,
  isDeployed: true,
  taskPayload: {
    scheduleConfig: {
      cronExpr: '0 0 13 * * ?',
      inputDatasets: [],
      inputNodes: [],
      outputDatasets: [],
      type: 'SCHEDULED',
    },
    taskConfig: {
      globalIdFields: [],
      sourceDataSource: "2",
      sourceDatasetName: "ESG.randomDataset",
      targetDataSource: "6",
      targetDatasetName: "dm.just_a_random_test_worker",
      writeMode: "overwrite",
    },
  },
  taskTemplateName: 'DataSync',
  upstreamTaskDefinitions: [],
  lastUpdateTime: '2020-11-10T12:52:23.356Z',
  createTime: '2020-11-10T12:52:23.356Z',
  lastModifier: '106929110202187776',
  owner: '106929110202187776',
};

const sampleTaskDefinition2: TaskDefinition = {
  id: '112114706067492864',
  name: '从mongo导入: dm.just_a_random_test_worker_with_long_long_long_long_long_name',
  creator: '106929110202187776',
  isArchived: false,
  isDeployed: false,
  taskPayload: {
    scheduleConfig: {
      cronExpr: '0 0 13 * * ?',
      inputDatasets: [],
      inputNodes: [],
      outputDatasets: [],
      type: 'SCHEDULED',
    },
    taskConfig: {
      globalIdFields: [],
      sourceDataSource: "2",
      sourceDatasetName: "ESG.randomDataset",
      targetDataSource: "6",
      targetDatasetName: "dm.just_a_random_test_worker",
      writeMode: "overwrite",
    },
  },
  taskTemplateName: 'DataSync',
  upstreamTaskDefinitions: [],
  lastUpdateTime: '2020-11-10T12:52:23.356Z',
  createTime: '2020-11-10T12:52:23.356Z',
  lastModifier: '106929110202187776',
  owner: '106929110202187776',
};

export const TaskDAGNodeDemo = () => {
  return (
    <IntlProvider locale="en-US" messages={demoMessage}>
      <main
        style={{
          display: 'block',
          width: '100%',
          height: '400px',
          position: 'relative',
          padding: '16px',
          border: '1px solid #e0e0e0',
        }}
      >
        <div>
          <TaskDAGNode
            taskDefinition={sampleTaskDefinition}
            interoperable
          />
        </div>
        <div style={{ marginTop: '12px' }}>
          <TaskDAGNode
            taskDefinition={sampleTaskDefinition2}
            interoperable
          />
        </div>
      </main>
    </IntlProvider>
  );
};
