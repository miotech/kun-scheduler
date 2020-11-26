import React, { useCallback, useState } from 'react';
import { Meta } from '@storybook/react';
import LogUtils from '@/utils/logUtils';

import { IntlProvider } from '@@/core/umiExports';
import { WorkflowCanvas } from '@/components/Workflow/canvas/Canvas.component';
import { Drag } from '@/components/Workflow/drag/Drag.component';

import '@/global.less';
import { WorkflowNode } from '@/components/Workflow/Workflow.typings';
import { computeDragInclusive } from '@/components/Workflow/helpers/dragbox-inclusive';
import { TASK_DAG_NODE_HEIGHT, TASK_DAG_NODE_WIDTH } from '@/components/Workflow/Workflow.constants';
import produce from 'immer';

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
          nodes={[]}
          edges={[]}
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
          nodes={[]}
          edges={[]}
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

const exampleNodes: WorkflowNode[] = [
  {
    id: 'example-node-1',
    name: 'example-node-1',
    x: 50,
    y: 50,
    taskTemplateName: 'SparkSQL',
    isDeployed: false,
  },
  {
    id: 'example-node-2',
    name: 'example-node-2',
    x: 350,
    y: 150,
    taskTemplateName: 'SparkSQL',
    isDeployed: false,
  },
  {
    id: 'example-node-3',
    name: 'example-node-3',
    x: 650,
    y: 50,
    taskTemplateName: 'Bash',
    isDeployed: false,
  },
];

export const WithDragAndBoxSelect = () => {
  const [ nodeStates, setNodeStates ] = useState(exampleNodes);

  const handleDragMove = useCallback(({ x, y, dx, dy }) => {
    const nextState = produce(nodeStates, draftState => {
      logger.trace('on drag move, x = %o, y = %o', x, y);
      // eslint-disable-next-line no-restricted-syntax
      for (const node of draftState) {
        const isSelected = computeDragInclusive({
          dragStartX: Math.min(x, x + dx),
          dragEndX: Math.max(x, x + dx),
          dragStartY: Math.min(y, y + dy),
          dragEndY: Math.max(y, y + dy),
        }, {
          x: node.x,
          y: node.y,
          width: TASK_DAG_NODE_WIDTH,
          height: TASK_DAG_NODE_HEIGHT,
        });
        if (isSelected) {
          logger.trace('node id = %o, state = selected', node.id);
          node.status = 'selected';
        } else {
          node.status = 'normal';
        }
      }
    });
    setNodeStates(nextState);
  }, [
    nodeStates,
  ]);

  return (
    <IntlProvider locale="en-US" messages={demoMessage}>
      <main style={mainStyle as any}>
        <WorkflowCanvas
          id="demo-canvas-1"
          width={1280}
          height={720}
          nodes={nodeStates}
          edges={[]}
        >
          <Drag
            width={1280}
            height={720}
            onDragStart={({ x, y }) => {
              logger.trace('on drag start, x = %o, y = %o', x, y);
            }}
            onDragMove={handleDragMove}
            onDragEnd={({ x, y, dx, dy }) => {
              logger.trace('on drag end, x = %o, y = %o; dx = %o, dy = %o', x, y, dx, dy);
            }}
          />
        </WorkflowCanvas>
      </main>
    </IntlProvider>
  );
};

export const WithZoomPan = () => {
  const [ nodeStates, /* setNodeStates */ ] = useState(exampleNodes);

  return (
    <IntlProvider locale="en-US" messages={demoMessage}>
      <main style={mainStyle as any}>
        <WorkflowCanvas
          id="demo-canvas-1"
          width={1280}
          height={720}
          nodes={nodeStates}
          edges={[]}
          zoomable
          onNodeClick={(workflowNode) => {
            logger.debug(`Clicked node: %o`, workflowNode);
          }}
        />
      </main>
    </IntlProvider>
  );
};
