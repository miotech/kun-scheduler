import React, { memo, useCallback, useEffect, useRef, useState } from 'react';
// import useRedux from '@/hooks/useRedux';
import { WorkflowCanvas } from '@/components/Workflow/canvas/Canvas.component';
import { useSize } from 'ahooks';
import LogUtils from '@/utils/logUtils';
import { convertTaskDefinitionsToGraph } from '@/pages/data-development/helpers/taskDefsToGraph';

import { TaskDefinition } from '@/definitions/TaskDefinition.type';
import {
  WorkflowNode,
  WorkflowEdge,
  Transform,
} from '@/components/Workflow/Workflow.typings';

import { computeDragInclusive } from '@/components/Workflow/helpers/dragbox-inclusive';
import {
  TASK_DAG_NODE_HEIGHT,
  TASK_DAG_NODE_WIDTH,
} from '@/components/Workflow/Workflow.constants';
import { Tool, ViewerMouseEvent } from 'react-svg-pan-zoom';
import { TOOL_BOX_SELECT } from '@/components/Workflow/toolbar/WorkflowDAGToolbar.component';
import styles from './TaskDAG.module.less';

interface OwnProps {
  taskDefinitions: TaskDefinition[];
  selectedTaskDefIds: string[];
  setSelectedTaskDefIds?: (taskDefIds: string[]) => any;
  viewportResetHookValue?: number;
  panzoomTool?: Tool | TOOL_BOX_SELECT;
  setPanzoomTool?: (nextTool: Tool | TOOL_BOX_SELECT) => any;
}

type Props = OwnProps;

export const logger = LogUtils.getLoggers('TaskDAG');

export const TaskDAG: React.FC<Props> = memo(function TaskDAG(props) {
  const {
    taskDefinitions = [],
    selectedTaskDefIds = [],
    setSelectedTaskDefIds = () => {},
    viewportResetHookValue,
  } = props;
  const [nodes, setNodes] = useState<WorkflowNode[]>([]);
  const [edges, setEdges] = useState<WorkflowEdge[]>([]);
  const [graphWidth, setGraphWidth] = useState<number>(0);
  const [graphHeight, setGraphHeight] = useState<number>(0);

  const handleDragMove = useCallback(
    ({ x, y, dx, dy }, canvasTransform: Transform) => {
      const selectedNodeIds = [];
      const nodeWidth = TASK_DAG_NODE_WIDTH * canvasTransform.scaleX;
      const nodeHeight = TASK_DAG_NODE_HEIGHT * canvasTransform.scaleY;
      // eslint-disable-next-line
      for (const node of nodes) {
        const isSelected = computeDragInclusive(
          {
            dragStartX: Math.min(x, x + dx),
            dragEndX: Math.max(x, x + dx),
            dragStartY: Math.min(y, y + dy),
            dragEndY: Math.max(y, y + dy),
          },
          {
            x: node.x * canvasTransform.scaleX + canvasTransform.transformX,
            y: node.y * canvasTransform.scaleY + canvasTransform.transformY,
            width: nodeWidth,
            height: nodeHeight,
          },
        );
        if (isSelected) {
          logger.trace('node id = %o, state = selected', node.id);
          selectedNodeIds.push(node.id);
        }
      }
      logger.trace('selectedNodeIds = %o', selectedNodeIds);
      setSelectedTaskDefIds(selectedNodeIds);
    },
    [nodes, setSelectedTaskDefIds],
  );

  useEffect(() => {
    if (setSelectedTaskDefIds) {
      setSelectedTaskDefIds([]);
    }
    if (taskDefinitions) {
      const {
        nodes: computedNodes,
        edges: computedEdges,
        graphWidth: computedGraphWidth,
        graphHeight: computedGraphHeight,
      } = convertTaskDefinitionsToGraph(taskDefinitions, selectedTaskDefIds);
      setNodes(computedNodes);
      setEdges(computedEdges);
      setGraphWidth(computedGraphWidth);
      setGraphHeight(computedGraphHeight);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [taskDefinitions]);

  useEffect(
    () => {
      const nextNodes = [...nodes];
      // eslint-disable-next-line no-plusplus
      for (let i = 0; i < nextNodes.length; i++) {
        const node = nextNodes[i];
        if (selectedTaskDefIds.indexOf(node.id) >= 0) {
          node.status = 'selected';
        } else {
          node.status = 'normal';
        }
      }
      setNodes(nextNodes);
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [selectedTaskDefIds],
  );

  const containerRef = useRef<any>();
  const { width, height } = useSize(containerRef);

  const handleNodeClick = useCallback(
    (workflowNode: WorkflowNode, multiselectMode?: boolean) => {
      if (multiselectMode) {
        if (selectedTaskDefIds.indexOf(workflowNode.id) >= 0) {
          setSelectedTaskDefIds(
            selectedTaskDefIds.filter(
              taskDefId => taskDefId !== workflowNode.id,
            ),
          );
        } else {
          setSelectedTaskDefIds([...selectedTaskDefIds, workflowNode.id]);
        }
      } else {
        setSelectedTaskDefIds([workflowNode.id]);
      }
    },
    [selectedTaskDefIds, setSelectedTaskDefIds],
  );

  const handleCanvasClick = useCallback(
    (ev: ViewerMouseEvent<any>) => {
      ev.stopPropagation();
      setSelectedTaskDefIds([]);
    },
    [setSelectedTaskDefIds],
  );

  return (
    <div
      id="app-task-dag-container"
      ref={containerRef}
      className={styles.TaskDAGContainer}
      data-tid="task-dag-container"
    >
      <WorkflowCanvas
        width={(width || 3) - 2}
        height={(height || 3) - 2}
        nodes={nodes}
        edges={edges}
        graphWidth={graphWidth}
        graphHeight={graphHeight}
        onCanvasClick={handleCanvasClick}
        onDragMove={handleDragMove}
        onNodeClick={handleNodeClick}
        viewportResetHookValue={viewportResetHookValue}
        panzoomTool={props.panzoomTool}
        setPanzoomTool={props.setPanzoomTool}
      />
    </div>
  );
});
