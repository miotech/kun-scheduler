import React, { memo, useRef } from 'react';
import c from 'clsx';
// import { useWindowSize } from '@react-hook/window-size';
import panzoom, { PanZoomOptions } from 'panzoom';

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
  /** zoomable */
  zoom?: boolean | PanZoomOptions;
  /** on click node */
  onNodeClick?: (workflowNode: WorkflowNode) => any;
}

type Props = OwnProps;

function useZoom<E extends HTMLElement>(options: PanZoomOptions = {}) {
  // const [width, height] = useWindowSize();

  const ref = React.useCallback((element: E) => {
    if (!element) return undefined;
    const zoom = panzoom(element, {
      ...options,
    });
    return () => {
      zoom.dispose();
    };
  }, []);

  return {
    ref,
  };
}

export const WorkflowCanvas: React.FC<Props> = memo(function WorkflowCanvas(props) {
  const {
    id,
    className,
    width = '100%',
    height = '100%',
    children,
    nodes = [],
    // edges = [],
    zoom = false,
    onNodeClick,
  } = props;

  const svgElementsGroupRef = useRef<any>();
  const { ref: svgElementsGroupZoomRef } = useZoom<any>((typeof zoom === 'object') ? zoom : {});

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
      <g data-tid="elements-group" ref={zoom ? svgElementsGroupZoomRef : svgElementsGroupRef}>
        {/* Render Nodes */}
        <NodeRenderer
          nodes={nodes}
          onNodeClick={onNodeClick}
        />
        {/* Plugins */}
        {children}
      </g>
    </svg>
  );
});
