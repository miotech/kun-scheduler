import React, { memo } from 'react';
import c from 'clsx';
import { WorkflowEdge, WorkflowNode } from '@/components/Workflow/Workflow.typings';

import './Canvas.global.less';
import { NodeRenderer } from '@/components/Workflow/node/NodeRenderer.component';

interface OwnProps {
  /** canvas width */
  width?: number | string;
  /** canvas height */
  height?: number | string;
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
}

type Props = OwnProps;

export const WorkflowCanvas: React.FC<Props> = memo(function WorkflowCanvas(props) {
  const {
    id,
    className,
    width = '100%',
    height = '100%',
    children,
    nodes = [],
    // edges = [],
  } = props;

  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      id={id}
      className={c('workflow-canvas', className)}
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
      {/* Render Nodes */}
      <NodeRenderer
        nodes={nodes}
      />
      {/* Plugins */}
      {children}
    </svg>
  );
});
