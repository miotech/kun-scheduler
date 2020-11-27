import React, { memo, useRef, useState } from 'react';
import c from 'clsx';
import { MODE_PANNING, ReactSVGPanZoom, Tool, TOOL_PAN, Value as ReactSVGPanZoomValue } from 'react-svg-pan-zoom';
import { NodeRenderer } from '@/components/Workflow/node/NodeRenderer.component';

import { Transform, WorkflowEdge, WorkflowNode } from '@/components/Workflow/Workflow.typings';

import './Canvas.global.less';

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
}

type Props = OwnProps;

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
    // edges = [],
    onNodeClick,
  } = props;

  const reactSVGPanZoomRef = useRef<any>();

  const [ panzoomValue, setPanzoomValue ] = useState<ReactSVGPanZoomValue>({
    version: 2,
    mode: MODE_PANNING,
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
    endX: null,
    endY: null,
    miniatureOpen: true,
  });
  const [ panzoomTool, setPanzoomTool ] = useState<Tool>(TOOL_PAN);

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
            cx="10.7991175%"
            cy="11.7361177%"
            fx="10.7991175%"
            fy="11.7361177%"
            r="148.107834%"
            gradientTransform="translate(0.107991,0.117361),scale(0.750000,1.000000),rotate(36.579912),translate(-0.107991,-0.117361)"
            id="canvasGradient"
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
        tool={panzoomTool}
        value={panzoomValue}
        onChangeTool={tool => {  setPanzoomTool(tool); }}
        onChangeValue={value => { setPanzoomValue(value); }}
        background="transparent"
        SVGBackground="transparent"
        scaleFactorMax={2.0}
        scaleFactorMin={0.1}
      >
        <svg data-tid="elements-group" width={Math.max(graphWidth, width)} height={Math.max(graphHeight, height)}>
          {/* Render Nodes */}
          <NodeRenderer
            nodes={nodes}
            onNodeClick={onNodeClick}
          />
        </svg>
      </ReactSVGPanZoom>
      {/* Plugins */}
      {React.Children.map(children, ((child, index) => {
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
      }))}
    </div>
  );
});
