import React from 'react';
import { Meta } from '@storybook/react';
import LogUtils from '@/utils/logUtils';

import { IntlProvider } from '@@/core/umiExports';
import { WorkflowCanvas } from '@/components/Workflow/canvas/Canvas.component';
import { Drag } from '@/components/Workflow/drag/Drag.component';

import '@/global.less';

export default {
  title: 'components/WorkflowDAG',
  component: WorkflowCanvas,
} as Meta;

const logger = LogUtils.getLoggers('workflow-canvas-story');

const demoMessage = {};

const mainStyle = {
  display: 'block',
  width: '100%',
  height: '100%',
  position: 'relative',
  padding: '16px',
  border: '1px solid #e0e0e0',
};

export const WorkflowCanvasDemo = () => {
  return (
    <IntlProvider locale="en-US" messages={demoMessage}>
      <main style={mainStyle as any}>
        <WorkflowCanvas
          id="demo-canvas-1"
          width={1280}
          height={720}
         />
      </main>
    </IntlProvider>
  );
};

export const WithDrag = () => {
  return (
    <IntlProvider locale="en-US" messages={demoMessage}>
      <main style={mainStyle as any}>
        <WorkflowCanvas
          id="demo-canvas-1"
          width={1280}
          height={720}
        >
          <Drag
            width={1280}
            height={720}
            onDragStart={({ x, y }) => {
              logger.debug('on drag start, x = %o, y = %o', x, y);
            }}
            onDragEnd={({ x, y, dx, dy }) => {
              logger.debug('on drag end, x = %o, y = %o; dx = %o, dy = %o', x, y, dx, dy);
            }}
          />
        </WorkflowCanvas>
      </main>
    </IntlProvider>
  );
};
