import React from 'react';
import { Meta } from '@storybook/react';
import { TaskViewsAside } from '@/pages/data-development/components/TaskViewsAside/TaskViewsAside';
import { IntlProvider } from '@@/core/umiExports';
import { TaskDefinitionView } from '@/definitions/TaskDefinition.type';



export default {
  title: 'components/DataDevelopment/TaskViewsAside',
  component: TaskViewsAside,
} as Meta;

const demoMessage = {};

const demoViews: TaskDefinitionView[] = [
  {
    id: '108468000000',
    name: 'Task View 1',
    creator: 0,
    taskDefIds: ['1', '2', '3', '4'],
  },
  {
    id: '108468000001',
    name: 'Task View 2',
    creator: 0,
    taskDefIds: ['5', '6', '7'],
  },
  {
    id: '108468000002',
    name: 'Task View 3 with long long name',
    creator: 0,
    taskDefIds: ['7', '8'],
  },
];

export const DatasetNodeCardDemo = () => {
  return (
    <IntlProvider locale="en-US" messages={demoMessage}>
      <main
        style={{
          display: 'flex',
          width: '100%',
          height: '100vh',
          flexDirection: 'column',
          border: '1px solid #e0e0e0',
        }}>
        <div style={{ width: '250px', flex: '1 1', position: 'relative' }} data-tid="pseudo-wrapper">
          <TaskViewsAside
            views={[]}
          />
        </div>
      </main>
    </IntlProvider>
  );
};
