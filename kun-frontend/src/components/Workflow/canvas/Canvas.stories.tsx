import React from 'react';
import { Meta } from '@storybook/react';

import { IntlProvider } from '@@/core/umiExports';
import { WorkflowCanvas } from '@/components/Workflow/canvas/Canvas.component';

import '@/global.less';

export default {
  title: 'components/WorkflowDAG',
  component: WorkflowCanvas,
} as Meta;

const demoMessage = {};

export const WorkflowCanvasDemo = () => {
  return (
    <IntlProvider locale="en-US" messages={demoMessage}>
      <main
        style={{
          display: 'block',
          width: '100%',
          height: '100%',
          position: 'relative',
          padding: '16px',
          border: '1px solid #e0e0e0',
        }}
      >
        <WorkflowCanvas
          id="demo-canvas-1"
          width={1280}
          height={720}
        />
      </main>
    </IntlProvider>
  );
};
