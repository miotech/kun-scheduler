import React from 'react';
import { Meta } from '@storybook/react';
import { TaskViewsAside } from '@/pages/data-development/components/TaskViewsAside/TaskViewsAside';
import { IntlProvider } from '@@/core/umiExports';

export default {
  title: 'components/DataDevelopment/TaskViewsAside',
  component: TaskViewsAside,
} as Meta;

const demoMessage = {};

export const DatasetNodeCardDemo = () => {
  return (
    <IntlProvider locale="en-US" messages={demoMessage}>
      <main style={{ display: 'flex', width: '100%', height: '100vh', flexDirection: 'column' }}>
        <div style={{ width: '240px', flex: '1 1', position: 'relative' }} data-tid="fake-wrapper">
          <TaskViewsAside

          />
        </div>
      </main>
    </IntlProvider>
  );
};
