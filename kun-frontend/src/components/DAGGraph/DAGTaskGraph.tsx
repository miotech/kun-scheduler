import React, { RefObject, useCallback, useEffect, useMemo, useRef, useState } from 'react';
import dagre from 'dagre';
import isNil from 'lodash/isNil';
import { PlusOutlined, MinusOutlined, FullscreenOutlined, FullscreenExitOutlined } from '@ant-design/icons';
import { Zoom } from '@visx/zoom';
import { localPoint } from '@visx/event';
import { Graph } from '@visx/network';
import { TaskNode, TaskRelation } from '@/components/DAGGraph/typings';
import LogUtils from '@/utils/logUtils';
import { DAGTaskNode } from '@/components/DAGGraph/DAGTaskNode';
import { DAGTaskEdge } from '@/components/DAGGraph/DAGTaskEdge';
import { TaskDefinition } from '@/definitions/TaskDefinition.type';
import { EventEmitter } from 'ahooks/lib/useEventEmitter';
import { DagState } from '@/rematch/models/dag';
import useRedux from '@/hooks/useRedux';
import './index.less';
import { useMount, useUnmount, useFullscreen, useSize } from 'ahooks';

export interface DAGTaskGraphProps {
  nodes: TaskNode[];
  relations: TaskRelation[];
  width?: number;
  height?: number;
  viewportCenter$?: EventEmitter<string | number>;
  centerTaskId?: string;
  expandUpstreamDAG: (id: string) => Promise<void>;
  expandDownstreamDAG: (id: string) => Promise<void>;
  closeUpstreamDag: (id: string) => void;
  closeDownstreamDag: (id: string) => void;
}

const DEFAULT_WIDTH = 150;
const DEFAULT_HEIGHT = 90;

export const DAGTaskGraph: React.FC<DAGTaskGraphProps> = props => {
  const {
    nodes,
    relations,
    width = 1024,
    height = 768,
    centerTaskId,
    expandUpstreamDAG,
    expandDownstreamDAG,
    closeUpstreamDag,
    closeDownstreamDag,
  } = props;
  const fullScreenRef = useRef(null);
  const [isFullscreen, { enterFullscreen, exitFullscreen }] = useFullscreen(fullScreenRef);
  const size = useSize(fullScreenRef);
  let dagHeight = height || 1024;
  let dagWidth = width || 768;
  if (isFullscreen) {
    dagHeight = size?.height || 0;
    dagWidth = size?.width || 0;
  }

  /* Here offsetX and offsetY are applied to centering DAG position */
  const [offsetX, setOffsetX] = useState<number>(0);
  const [offsetY, setOffsetY] = useState<number>(0);
  const zoomRef: RefObject<Zoom> = useRef<Zoom>(null);

  const graphWrapperRef = useRef() as RefObject<SVGGElement>;
  const requestAnimationRef = React.useRef<number>();
  const graphContainerRef: React.MutableRefObject<SVGGElement | undefined> = React.useRef();

  const logger = useMemo(() => LogUtils.getLoggers('DAGTaskGraph'), []);
  const { selector } = useRedux<DagState>(state => state.dag);
  const { currentClickId } = selector;

  const animate = () => {
    const nextZoomStr = zoomRef.current?.toString() || 'matrix(1, 0, 0, 1, 0, 0)';
    // setZoomStr(nextZoomStr);
    if (graphContainerRef.current) {
      graphContainerRef.current.setAttribute('transform', nextZoomStr);
    }
    requestAnimationRef.current = requestAnimationFrame(animate);
  };

  useMount(() => {
    requestAnimationRef.current = requestAnimationFrame(animate);
  });

  useUnmount(() => {
    if (requestAnimationRef.current) {
      cancelAnimationFrame(requestAnimationRef.current);
    }
  });

  /**
   * Graph includes position information computed by nodes and edges with dagre layout,
   * should be updated only when task nodes and relations changes
   */
  const graph: dagre.graphlib.Graph<{ data?: TaskDefinition }> = useMemo(() => {
    const g = new dagre.graphlib.Graph({
      directed: true,
    });
    // Set an object for the graph label
    g.setGraph({});
    // Default to assigning a new object as a label for each new edge.
    g.setDefaultEdgeLabel(() => ({}));
    // Add task nodes and relations into graph
    nodes.forEach(node => {
      g.setNode(node.id, {
        label: node.name,
        width: DEFAULT_WIDTH,
        height: DEFAULT_HEIGHT,
        data: {
          ...node.data,
          isCenter: centerTaskId === node.id,
        },
      });
    });
    relations.forEach(relation => {
      if (
        nodes.findIndex(n => n.id === relation.downstreamTaskId) >= 0 &&
        nodes.findIndex(n => n.id === relation.upstreamTaskId) >= 0
      ) {
        g.setEdge(relation.upstreamTaskId, relation.downstreamTaskId);
      }
    });
    // compute dagre layout
    logger.trace('g = %o', g);
    dagre.layout(g, {
      rankdir: 'TB', // Top-to-bottom
      nodesep: DEFAULT_WIDTH * 2.0,
      ranksep: DEFAULT_HEIGHT * 3.0,
      ranker: 'tight-tree',
    });
    return g;
  }, [centerTaskId, nodes, relations, logger]);

  const vxGraphData = useMemo(() => {
    return {
      nodes: graph
        .nodes()
        .map(nodeName => {
          // logger.debug('nodeName = %o; graph.node(nodeName) = %o', nodeName, graph.node(nodeName));
          const graphNode = graph.node(nodeName);
          if (!graphNode) {
            return null;
          }
          return {
            x: graphNode.x || 0,
            y: graphNode.y || 0,
            width: DEFAULT_WIDTH,
            height: DEFAULT_HEIGHT,
            title: graphNode.label || '',
            data: graphNode.data || {},
          };
        })
        .filter(n => !isNil(n)),
      links: graph
        .edges()
        .map(edge => {
          const graphEdge = graph.edge(edge);
          if (!graphEdge) {
            return null;
          }
          return {
            source: graphEdge.points[0],
            target: graphEdge.points[graphEdge.points.length - 1],
            path: graphEdge.points,
          };
        })
        .filter(e => !isNil(e)),
    };
  }, [graph]);

  /* Update position offset after reference changes */
  useEffect(() => {
    const nodeId = currentClickId || centerTaskId;
    const centerNode = graph.node(nodeId);
    if (centerNode) {
      const { x, y } = centerNode;
      zoomRef?.current?.setTransformMatrix({
        scaleX: 1,
        scaleY: 1,
        skewX: 0,
        skewY: 0,
        translateX: 0,
        translateY: 0,
      });
      const nodeWidth = centerNode?.width;
      const nodeHeight = centerNode?.height;
      setOffsetX(x - nodeWidth / 2);
      setOffsetY(y - nodeHeight / 2);
    }
  }, [graph, currentClickId, centerTaskId,isFullscreen, zoomRef]);

  if (props.viewportCenter$) {
    // when receiving place center event
    props.viewportCenter$.useSubscription(nodeId => {
      logger.trace('Placing view point center to nodeId: %o', nodeId);
      logger.trace('zoomRef.current =', zoomRef.current);
      const { x, y } = graph.node(`${nodeId}`);
      logger.trace('Placing view point center to: [%o, %o]', x, y);
      if (zoomRef.current) {
        zoomRef.current.center();
        zoomRef.current.reset();
        // zoomRef.current.translateTo({x, y});
        zoomRef.current.setTransformMatrix({
          scaleX: 1,
          scaleY: 1,
          skewX: 0,
          skewY: 0,
          // TODO: need more precise relocation
          translateX: -x + width / 4,
          translateY: -y,
        });
      }
    });
  }

  // customized zoom in/out event on mouse wheel scroll
  const wheelDeltaEventCallback = useCallback((ev: React.WheelEvent | WheelEvent) => {
    const deltaBase = {
      translateX: 0,
      translateY: 0,
      skewX: 0,
      skewY: 0,
    };
    if (ev.deltaY < 0) {
      return {
        ...deltaBase,
        scaleX: 1.025,
        scaleY: 1.025,
      };
    }
    // else
    return {
      ...deltaBase,
      scaleX: 0.9756097560975611, // 1 / 1.025
      scaleY: 0.9756097560975611,
    };
  }, []);
  const graphDOM = useMemo(() => {
    return (
      <Graph
        left={-(offsetX - dagWidth / 2 + 75)}
        top={-(offsetY - dagHeight / 2 + 45)}
        graph={vxGraphData}
        linkComponent={DAGTaskEdge as any}
        nodeComponent={({ node }) => (
          <DAGTaskNode
            node={node}
            expandUpstreamDAG={expandUpstreamDAG}
            expandDownstreamDAG={expandDownstreamDAG}
            closeUpstreamDag={closeUpstreamDag}
            closeDownstreamDag={closeDownstreamDag}
          />
        )}
      />
    );
  }, [
    vxGraphData,
    offsetX,
    offsetY,
    dagWidth,
    dagHeight,
    expandUpstreamDAG,
    expandDownstreamDAG,
    closeUpstreamDag,
    closeDownstreamDag,
  ]);

  const renderDragMaskRect = useCallback(
    zoom => (
      <rect
        width={dagWidth}
        height={dagHeight}
        rx={14}
        fill="transparent"
        onTouchStart={zoom.dragStart}
        onTouchMove={zoom.dragMove}
        onTouchEnd={zoom.dragEnd}
        onMouseDown={zoom.dragStart}
        onMouseMove={zoom.dragMove}
        onMouseUp={zoom.dragEnd}
        onMouseLeave={() => {
          if (zoom.isDragging) zoom.dragEnd();
        }}
        onDoubleClick={event => {
          const point = localPoint(event) || { x: 0, y: 0 };
          zoom.scale({ scaleX: 1.5, scaleY: 1.5, point });
        }}
      />
    ),
    [dagWidth, dagHeight],
  );

  return (
    <div ref={fullScreenRef} >
      <div className="dag-button-group">
        <button
          type="button"
          className="dag-util-btn"
          onClick={() => {
            if (zoomRef.current) {
              zoomRef.current.scale({ scaleX: 1.025, scaleY: 1.025 });
            }
          }}
        >
          <PlusOutlined />
        </button>
        <button
          type="button"
          className="dag-util-btn"
          onClick={() => {
            if (zoomRef.current) {
              zoomRef.current.scale({
                scaleX: 0.9756097560975611, // 1 / 1.025
                scaleY: 0.9756097560975611,
              });
            }
          }}
        >
          <MinusOutlined />
        </button>
        {!isFullscreen &&
          <button
            type="button"
            onClick={enterFullscreen}
            className="dag-util-btn">
            <FullscreenOutlined />
          </button>
        }
        {isFullscreen &&
          <button
            type="button"
            onClick={exitFullscreen}
            className="dag-util-btn">
            <FullscreenExitOutlined />
          </button>
        }
      </div>
      <Zoom ref={zoomRef} width={dagWidth} height={dagHeight} wheelDelta={wheelDeltaEventCallback}>
        {zoom => {
          return (
            <svg
              width={dagWidth}
              height={dagHeight}
              style={{
                cursor: zoom.isDragging ? 'grabbing' : 'grab',
                // overflow: zoom.isDragging ? 'overlay' : 'hidden',
              }}
            >
              <defs>
                <marker
                  id="arrowEnd"
                  viewBox="0 0 10 10"
                  refX="10"
                  refY="5"
                  markerUnits="strokeWidth"
                  markerWidth="10"
                  markerHeight="10"
                  orient="auto"
                >
                  <path d="M 0 0 L 10 5 L 0 10 z" fill="#7A7E87" />
                </marker>
              </defs>
              <rect width={dagWidth} height={dagHeight} rx={14} fill="#FFF" />

              {/* When not dragging, place drag mask behind */}
              {renderDragMaskRect(zoom)}
              {/* {!zoom.isDragging ? renderDragMaskRect(zoom) : null} */}

              <g>
                <g
                  // @ts-ignore
                  ref={graphContainerRef}
                  /* Optimization:
                     do not set transform attribute through react re-rendering,
                     instead we use reference and DOM setAttribute to update this matrix value
                  */
                  /* transform={zoomStr} */
                  width={dagWidth}
                  height={dagHeight}
                >
                  <g id="realgraph" ref={graphWrapperRef}>
                    {graphDOM}
                  </g>
                </g>
              </g>

              {/* When dragging, place drag mask front */}
              {/* {zoom.isDragging ? renderDragMaskRect(zoom) : null} */}
            </svg>
          );
        }}
      </Zoom>
    </div>
  );
};
