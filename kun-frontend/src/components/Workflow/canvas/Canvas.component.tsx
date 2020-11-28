import React, { memo, useRef, useState } from 'react';
import c from 'clsx';
import LogUtils from '@/utils/logUtils';

import {
  MODE_IDLE, ReactSVGPanZoom, Tool, TOOL_AUTO, Value as ReactSVGPanZoomValue, ViewerMouseEvent
} from 'react-svg-pan-zoom';
import { NodeRenderer } from '@/components/Workflow/node/NodeRenderer.component';
import { TOOL_BOX_SELECT, WorkflowDAGToolbar } from '@/components/Workflow/toolbar/WorkflowDAGToolbar.component';

import { Transform, WorkflowEdge, WorkflowNode } from '@/components/Workflow/Workflow.typings';

import './Canvas.global.less';
import { Drag } from '@/components/Workflow/drag/Drag.component';
import { EdgeRenderer } from '@/components/Workflow/edge/EdgeRenderer.component';

interface OwnProps {
  /** canvas width */
  width?: number;
  /** canvas height */
  height?: number;
  /** DAG graph svg width */
  graphWidth?: number;
  /** DAG graph svg height */
  graphHeight?: number;
  /** HTML element id */
  id?: string;
  /** CSS classname */
  className?: string;
  /** children elements */
  children?: React.ReactNode;
  /** nodes */
  nodes: WorkflowNode[];
  /** edges */
  edges: WorkflowEdge[];
  /** on click node */
  onNodeClick?: (workflowNode: WorkflowNode) => any;
  /** on Canvas click */
  onCanvasClick?: (ev: ViewerMouseEvent<any>) => any;
}

type Props = OwnProps;

export const logger = LogUtils.getLoggers('WorkflowCanvas');

export const WorkflowCanvas: React.FC<Props> = memo(function WorkflowCanvas(props) {
  const {
    id,
    className,
    width = 800,
    height = 600,
    graphWidth = width,
    graphHeight = height,
    children,
    nodes = [],
    edges = [],
    onNodeClick,
  } = props;

  const reactSVGPanZoomRef = useRef<any>();

  const [ panzoomValue, setPanzoomValue ] = useState<ReactSVGPanZoomValue>({
    version: 2,
    mode: MODE_IDLE,
    focus: false,
    a: 1,
    b: 0,
    c: 0,
    d: 1,
    e: 0,
    f: 0,
    viewerWidth: width || 0,
    viewerHeight: height || 0,
    SVGWidth: Math.max(graphWidth, width) || 0,
    SVGHeight: Math.max(graphHeight, height) || 0,
    startX: null,
    startY: null,
    endX: Math.max(graphWidth, width),
    endY: Math.max(graphHeight, height),
    miniatureOpen: true,
  });
  const [ panzoomTool, setPanzoomTool ] = useState<Tool | TOOL_BOX_SELECT>(TOOL_AUTO);

  return (
    <div className="workflow-canvas-container" style={{ width, height }}>
      <svg
        xmlns="http://www.w3.org/2000/svg"
        id={id}
        className={c('workflow-canvas-bg', className)}
        width={width}
        height={height}
      >
        {/* Canvas svg constant definitions */}
        <defs>
          <radialGradient
            id="canvasGradient"
            cx="10.7991175%"
            cy="11.7361177%"
            fx="10.7991175%"
            fy="11.7361177%"
            r="148.107834%"
            gradientTransform="translate(0.107991,0.117361),scale(0.750000,1.000000),rotate(36.579912),translate(-0.107991,-0.117361)"
          >
            <stop stopColor="#EFEFEF" offset="0%" />
            <stop stopColor="#CCCCCC" offset="100%" />
          </radialGradient>
        </defs>
        {/* background with gradients */}
        <rect
          x="0"
          y="0"
          width="100%"
          height="100%"
          style={{ fill: 'url(#canvasGradient)' }}
        />
      </svg>
      <ReactSVGPanZoom
        ref={reactSVGPanZoomRef}
        width={width}
        height={height}
        tool={(panzoomTool === 'box-select') ? 'none' : panzoomTool}
        value={panzoomValue}
        onClick={props.onCanvasClick}
        onChangeTool={tool => { setPanzoomTool(tool); }}
        onChangeValue={value => { setPanzoomValue(value); }}
        background="none"
        SVGBackground="none"
        scaleFactorMax={2.0}
        scaleFactorMin={0.5}
        customToolbar={() => <></>}
      >
        <svg data-tid="elements-group" width={Math.max(graphWidth, width)} height={Math.max(graphHeight, height)}>
          {/* Background grid */}
          <defs>
            <pattern id="tenthGrid" width="10" height="10" patternUnits="userSpaceOnUse">
              <path d="M 10 0 L 0 0 0 10" fill="none" stroke="silver" strokeWidth="0.5"/>
            </pattern>
            <pattern id="grid" width="100" height="100" patternUnits="userSpaceOnUse">
              <rect width="100" height="100" fill="url(#tenthGrid)"/>
              <path d="M 100 0 L 0 0 0 100" fill="none" stroke="gray" strokeWidth="1"/>
            </pattern>
            <marker
              id="arrowEnd"
              viewBox="0 0 5 5"
              refX="0"
              refY="2.5"
              markerUnits="strokeWidth"
              markerWidth="5"
              markerHeight="5"
              orient="auto"
            >
              <path d="M 0 0 L 5 2.5 L 0 5 z" fill="#4869FC" />
            </marker>
          </defs>
          <rect
            x={-2 * Math.max(graphWidth, width)}
            y={-2 * Math.max(graphHeight, height)}
            width={Math.max(graphWidth, width) * 5}
            height={Math.max(graphHeight, height) * 5}
            style={{ fill: 'url(#grid)' }}
          />
          {/* Render Nodes */}
          <NodeRenderer
            nodes={nodes}
            onNodeClick={onNodeClick}
          />
          <EdgeRenderer
            nodes={nodes}
            edges={edges}
          />
        </svg>
      </ReactSVGPanZoom>
      {/* Plugins */}
      {React.Children.map(children, ((child) => {
        if (React.isValidElement(child)) {
          return React.cloneElement(child, {
            ...child.props,
            transform: {
              scaleX: panzoomValue.a,
              scaleY: panzoomValue.d,
              transformX: panzoomValue.e,
              transformY: panzoomValue.f,
            } as Transform,
          });
        }
        // else
        return null;
      }))}
      {/* Toolbar */}
      <WorkflowDAGToolbar
        currentTool={panzoomTool}
        onChangeTool={(nextTool) => {
          setPanzoomTool(nextTool);
        }}
        onClickReset={() => {
          reactSVGPanZoomRef.current?.reset();
        }}
      />
      {
        (() => {
          if (panzoomTool === 'box-select') {
            return (
              <Drag
                width={width}
                height={height}
                onDragStart={({ x, y }) => {
                  logger.trace('on drag start, x = %o, y = %o', x, y);
                }}
                onDragMove={() => {}}
                onDragEnd={({ x, y, dx, dy }) => {
                  logger.trace('on drag end, x = %o, y = %o; dx = %o, dy = %o', x, y, dx, dy);
                }}
              />
            );
          }
          // else
          return null;
        })()
      }
    </div>
  );
});
