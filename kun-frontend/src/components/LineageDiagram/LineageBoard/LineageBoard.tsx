import React, { memo, RefObject, useCallback, useMemo, useRef } from 'react';
import c from 'clsx';
import { useSize } from 'ahooks';
import dagre from 'dagre';
import { NodeGroup } from 'react-move';
import isNil from 'lodash/isNil';
import {
  DatasetNodeCard,
  PortStateType,
} from '@/components/LineageDiagram/DatasetNodeCard/DatasetNodeCard';
import LogUtils from '@/utils/logUtils';
import { KunSpin } from '@/components/KunSpin';
import { Group } from '@visx/group';
import { quadIn } from '@/utils/animation/ease/quad';
import { buildLineageEdgePath } from '@/components/LineageDiagram/LineageBoard/helpers/buildLineageEdgePath';
import { ProvidedZoom } from '@visx/zoom/lib/types';

import {
  NodeGroupElement,
  ReactMoveTiming,
} from '@/definitions/ReactMove.type';
import { LineageEdge, LineageNode } from '@/definitions/Lineage.type';
import { Dataset } from '@/definitions/Dataset.type';

import {
  EDGE_SEP_DEFAULT,
  NODE_DEFAULT_HEIGHT,
  NODE_DEFAULT_WIDTH,
  NODE_SEP_DEFAULT,
  PORT_WIDTH,
  RANK_SEP_DEFAULT,
} from './helpers/constants';

import './LineageBoard.less';

interface OwnProps {
  width?: number;
  height?: number;
  nodes: LineageNode[];
  edges: LineageEdge[];
  loading?: boolean;
  nodesep?: number;
  edgesep?: number;
  ranksep?: number;
  rankdir?: 'TB' | 'BT' | 'LR' | 'RL';
  align?: 'UL' | 'UR' | 'DL' | 'DR';
  ranker?: 'network-simplex' | 'tight-tree' | 'longest-path';
  onExpandUpstream?: (datasetId: string) => any;
  onExpandDownstream?: (datasetId: string) => any;
  onClickNode?: (
    node: LineageDagreNodeData,
    event: React.MouseEvent<any>,
  ) => any;
  onClickEdge?: (
    edgeInfo: { srcNodeId: string; destNodeId: string },
    event: React.MouseEvent<SVGPathElement>,
  ) => any;
  onClickBackground?: (event: React.MouseEvent<any>) => any;
  loadingStateNodeIds?: string[];
  zoom?: ProvidedZoom;
  onDragStart?: (ev: React.MouseEvent | React.TouchEvent) => any;
  onDragMove?: (ev: React.MouseEvent | React.TouchEvent) => any;
  onDragEnd?: (ev: React.MouseEvent | React.TouchEvent) => any;
  backgroundCursor?: string;
}

type Props = OwnProps;

export type LineageDagreNodeData = Dataset & {
  expandableUpstream?: boolean;
  expandableDownstream?: boolean;
  selected?: boolean;
  rowCount?: number;
};

type LineageDagreNode = dagre.Node<{
  id: string;
  data: LineageDagreNodeData;
}>;

type LineageNodeGroupElement = NodeGroupElement<
  LineageDagreNode,
  { x: number; y: number; opacity: number } & ReactMoveTiming
>;

type LineageEdgeGroupElement = NodeGroupElement<
  LineageEdge,
  {
    srcNodeX: number;
    srcNodeY: number;
    destNodeX: number;
    destNodeY: number;
    opacity: number;
    selected: boolean;
  } & ReactMoveTiming
>;

export const logger = LogUtils.getLoggers('LineageBoard');

export const LineageBoard: React.FC<Props> = memo(function LineageBoard(props) {
  const {
    nodes,
    edges,
    loading = false,
    nodesep = NODE_SEP_DEFAULT,
    edgesep = EDGE_SEP_DEFAULT,
    ranksep = RANK_SEP_DEFAULT,
    rankdir = 'LR',
    align = undefined,
    ranker = 'network-simplex',
    loadingStateNodeIds = [],
  } = props;

  const ref = useRef() as RefObject<HTMLDivElement>;
  const size = useSize(ref);

  const width = isNil(props.width) ? (size.width || 0) : props.width;
  const height = isNil(props.height) ? (size.height || 0) : props.height;

  const graph = useMemo(() => {
    const g = new dagre.graphlib.Graph();
    g.setGraph({
      ...{
        nodesep,
        edgesep,
        ranksep,
        rankdir,
        align,
        ranker,
      },
    });
    g.setDefaultEdgeLabel(() => ({}));
    (nodes || []).forEach(node => {
      g.setNode(node.id, {
        width: NODE_DEFAULT_WIDTH,
        height: NODE_DEFAULT_HEIGHT,
        ...node,
      });
    });
    (edges || []).forEach(edge => {
      g.setEdge(
        { v: edge.src, w: edge.dest },
        { selected: edge.selected || false },
      );
    });
    dagre.layout(g);
    logger.debug('generated graph =', g);
    return g;
  }, [nodes, edges, nodesep, edgesep, ranksep, rankdir, align, ranker]);

  const nodesData = useMemo(() => {
    return graph
      .nodes()
      .map((nodeId: string) => graph.node(nodeId) as LineageDagreNode);
  }, [graph]);

  const renderNodeGroupElement = useCallback(
    (nodeGroupElement: LineageNodeGroupElement) => {
      const { data: node, state } = nodeGroupElement;
      logger.trace(
        'In renderNodeGroupElement, nodeGroupElement =',
        nodeGroupElement,
      );
      return (
        <g key={node.id} data-node-id={node.id}>
          <foreignObject
            x={state.x}
            y={state.y}
            opacity={state.opacity}
            width={(node.width || NODE_DEFAULT_WIDTH) + PORT_WIDTH}
            height={node.height || NODE_DEFAULT_HEIGHT}
          >
            <div
              style={{ position: 'relative', left: '10px', cursor: 'pointer' }}
            >
              <DatasetNodeCard
                state={node.data?.selected ? 'selected' : 'default'}
                data={node.data}
                leftPortState={computeNodePortState(
                  node.id,
                  node.data?.expandableUpstream || false,
                  loadingStateNodeIds,
                )}
                rightPortState={computeNodePortState(
                  node.id,
                  node.data?.expandableDownstream || false,
                  loadingStateNodeIds,
                )}
                onExpandLeft={ev => {
                  ev.stopPropagation();
                  if (props.onExpandUpstream) {
                    props.onExpandUpstream(node.id);
                  }
                }}
                rowCount={node.data?.rowCount}
                onExpandRight={ev => {
                  ev.stopPropagation();
                  if (props.onExpandDownstream) {
                    props.onExpandDownstream(node.id);
                  }
                }}
                useNativeLink
                onClick={(ev: React.MouseEvent) => {
                  if (props.onClickNode) {
                    props.onClickNode(node.data, ev);
                  }
                }}
              />
            </div>
          </foreignObject>
        </g>
      );
    },
    [loadingStateNodeIds, props],
  );

  const renderLineageNodesGroup = () => {
    return (
      <NodeGroup
        data={nodesData}
        keyAccessor={(n: LineageDagreNode) => n.id}
        start={(n: LineageDagreNode, idx: number) => {
          logger.trace('In start fn, n =', n, '; idx =', idx);
          return {
            x: 0,
            y: 0,
            opacity: 1e-6,
          };
        }}
        enter={(n: LineageDagreNode, idx: number) => {
          logger.trace('In enter fn, n =', n, '; idx =', idx);
          return {
            // See: https://github.com/sghall/react-move#transitioning-state
            // `x: value` means rendering the node at x = value position immediately, with NO transit
            // `x: [value]` means gradual transit x from current position value to `value`
            // `x: [value1, value2] means gradual transit x from value1 to value2
            // `x: Function`: Function will be used as a custom tween function.
            opacity: [1],
            x: [n.x],
            y: [n.y],
            timing: { duration: 500, delay: 0 },
          };
        }}
        update={(n: LineageDagreNode, idx: number) => {
          logger.trace('In update fn, n =', n, '; idx =', idx);
          return {
            opacity: [1],
            x: [n.x],
            y: [n.y],
            timing: { duration: 500, ease: quadIn },
          };
        }}
      >
        {(renderingNodes: LineageNodeGroupElement[]) => (
          <Group>
            {renderingNodes.map(groupElement => {
              // logger.debug('groupElement =', groupElement);
              return renderNodeGroupElement(groupElement);
            })}
          </Group>
        )}
      </NodeGroup>
    );
  };

  const svgDefs = useMemo(
    () => (
      <defs>
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
          <path d="M 0 0 L 5 2.5 L 0 5 z" fill="#d8d8d8" />
        </marker>
        <marker
          id="arrowEndHover"
          viewBox="0 0 5 5"
          refX="0"
          refY="2.5"
          markerUnits="strokeWidth"
          markerWidth="5"
          markerHeight="5"
          orient="auto"
        >
          <path d="M 0 0 L 5 2.5 L 0 5 z" fill="rgba(26, 115, 232, 0.3)" />
        </marker>
        <marker
          id="arrowEndActive"
          viewBox="0 0 5 5"
          refX="0"
          refY="2.5"
          markerUnits="strokeWidth"
          markerWidth="5"
          markerHeight="5"
          orient="auto"
        >
          <path d="M 0 0 L 5 2.5 L 0 5 z" fill="#1a73e8" />
        </marker>
      </defs>
    ),
    [],
  );

  const renderLineageEdgesGroup = () => {
    const edgeData = graph.edges().map(e => ({
      src: e.v,
      dest: e.w,
      selected: graph.edge(e.v, e.w)?.selected || false,
    }));
    logger.trace('edgeData =', edgeData);
    return (
      <NodeGroup
        data={edgeData}
        keyAccessor={(e: LineageEdge) => `${e.src}-${e.dest}`}
        start={(e: LineageEdge, idx: number) => {
          logger.trace('In start fn, e =', e, '; idx =', idx);
          /*
          const [ fromNode, destNode ] = [
            graph.node(e.src) as LineageDagreNode,
            graph.node(e.dest) as LineageDagreNode,
          ];
          */
          return {
            opacity: 1e-6,
            srcNodeX: 0,
            srcNodeY: 0,
            destNodeX: 0,
            destNodeY: 0,
            selected: e.selected,
          };
        }}
        enter={(e: LineageEdge, idx: number) => {
          logger.trace('In enter fn, n =', e, '; idx =', idx);
          const [fromNode, destNode] = [
            graph.node(e.src) as LineageDagreNode,
            graph.node(e.dest) as LineageDagreNode,
          ];
          if (!fromNode || !destNode) {
            return {};
          }
          return {
            opacity: [1],
            srcNodeX: [fromNode.x],
            srcNodeY: [fromNode.y],
            destNodeX: [destNode.x],
            destNodeY: [destNode.y],
            selected: e.selected,
            timing: { duration: 500, delay: 0 },
          };
        }}
        update={(e: LineageEdge, idx: number) => {
          logger.trace('In update fn, n =', e, '; idx =', idx);
          const [fromNode, destNode] = [
            graph.node(e.src) as LineageDagreNode,
            graph.node(e.dest) as LineageDagreNode,
          ];
          if (!fromNode || !destNode) {
            return {};
          }
          return {
            opacity: [1],
            srcNodeX: [fromNode.x],
            srcNodeY: [fromNode.y],
            destNodeX: [destNode.x],
            destNodeY: [destNode.y],
            selected: e.selected,
            timing: { duration: 500, ease: quadIn },
          };
        }}
      >
        {(renderingEdges: LineageEdgeGroupElement[]) => (
          <Group>
            {renderingEdges.map(edge => {
              const { state } = edge;
              if (isNil(state.srcNodeX) || isNil(state.srcNodeY)) {
                return <></>;
              }
              logger.trace('in rendering edge =', edge);
              return (
                <path
                  className={c('lineage-edge-path', {
                    'lineage-edge-path--selected': state.selected,
                  })}
                  data-tid={`edge-${edge.data.src}-${edge.data.dest}`}
                  key={`edge-${edge.data.src}-${edge.data.dest}`}
                  d={buildLineageEdgePath(
                    { x: state.srcNodeX, y: state.srcNodeY },
                    { x: state.destNodeX, y: state.destNodeY },
                  )}
                  onClick={(ev: React.MouseEvent<SVGPathElement>) => {
                    if (props.onClickEdge) {
                      props.onClickEdge(
                        {
                          srcNodeId: edge.data.src,
                          destNodeId: edge.data.dest,
                        },
                        ev,
                      );
                    }
                  }}
                  stroke="#d8d8d8"
                  strokeWidth={2}
                  opacity={state.opacity}
                  fill="transparent"
                  markerEnd="url(#arrowEnd)"
                  cursor="pointer"
                  pointerEvents="all"
                />
              );
            })}
          </Group>
        )}
      </NodeGroup>
    );
  };


  return (
    <div className="lineage-board" ref={ref}>
      <KunSpin spinning={loading}>
        <svg
          width={width}
          height={height}
        >
          <rect
            data-tid="lineage-board-background"
            onClick={ev => {
              if (props.onClickBackground) {
                props.onClickBackground(ev);
              }
            }}
            onMouseDown={props?.onDragStart}
            onMouseMove={props?.onDragMove}
            onMouseUp={props?.onDragEnd}
            onTouchStart={props?.onDragStart}
            onTouchMove={props?.onDragMove}
            onTouchEnd={props?.onDragEnd}
            cursor={props.backgroundCursor || undefined}
            width={size.width || 0}
            height={size.height || 0}
            fill="transparent"
          />
          {svgDefs}
          <g
            transform={isNil(props.zoom) ? undefined : props.zoom.toString()}
          >
            {renderLineageEdgesGroup()}
            {/* Lineage nodes group */}
            {renderLineageNodesGroup()}
          </g>
        </svg>
      </KunSpin>
    </div>
  );
});

function computeNodePortState(
  id: string,
  expandable: boolean,
  loadingStateNodeIds: string[],
): PortStateType {
  if (loadingStateNodeIds.indexOf(id) >= 0) {
    return 'loading';
  }
  if (expandable) {
    return 'collapsed';
  }
  // else
  return 'hidden';
}
