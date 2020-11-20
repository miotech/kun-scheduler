import React from 'react';
import { Meta } from '@storybook/react';

import { IntlProvider } from '@@/core/umiExports';
import { TaskDefinitionTable } from './TaskDefinitionTable';

import '@/global.less';

export default {
  title: 'components/DataDevelopment/TaskDefinitionTable',
  component: TaskDefinitionTable,
} as Meta;

const demoMessage = {
  'dataDevelopment.definition.property.name': 'Task definition name',
  'dataDevelopment.definition.property.owner': 'Owner',
  'dataDevelopment.definition.property.createTime': 'Create time',
  'dataDevelopment.definition.property.lastUpdateTime': 'Last update time',
  'dataDevelopment.definition.property.isDeployed': 'Deployed',
};

export const TaskDefinitionTableDemo = () => {
  return (
    <IntlProvider locale="en-US" messages={demoMessage}>
      <main>
        <TaskDefinitionTable
          taskDefViewId=""
        />
      </main>
    </IntlProvider>
  );
};
