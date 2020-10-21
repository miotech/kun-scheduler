import React, { memo, RefObject, useMemo, useRef } from 'react';
import { useSize } from 'ahooks';
import dagre from 'dagre';
import { DatasetNodeCard } from '@/components/LineageDiagram/DatasetNodeCard/DatasetNodeCard';
import LogUtils from '@/utils/logUtils';

import { LineageEdge, LineageNode } from '@/definitions/Lineage.type';

import './LineageBoard.less';
import { Dataset } from '@/definitions/Dataset.type';
import { KunSpin } from '@/components/KunSpin';

interface OwnProps {
  nodes: LineageNode[];
  edges: LineageEdge[];
  loading?: boolean;
  nodesep?: number;
  edgesep?: number;
  ranksep?: number;
  rankdir?: 'TB' | 'BT' | 'LR' | 'RL';
  align?: 'UL' | 'UR' | 'DL' | 'DR';
  ranker?: 'network-simplex' | 'tight-tree' | 'longest-path';
}

type Props = OwnProps;

type LineageDagreNode = dagre.Node<{ id: string; data: Dataset }>;

const NODE_DEFAULT_WIDTH = 282;
const NODE_DEFAULT_HEIGHT = 110;
const NODE_SEP_DEFAULT = 100;
const EDGE_SEP_DEFAULT = 30;
const RANK_SEP_DEFAULT = 140;

function buildLineageEdgePath(points: { x: number, y: number}[], options?: {
  nodeWidth?: number, nodeHeight?: number
}): string {
  const {
    nodeWidth = NODE_DEFAULT_WIDTH,
    nodeHeight = NODE_DEFAULT_HEIGHT,
  } = (options || {});
  const start = {
    x: points[0].x + (nodeWidth / 2),
    y: points[0].y + (nodeHeight / 2),
  };
  const end = {
    x: points[points.length - 1].x + (nodeWidth / 2),
    y: points[points.length - 1].y + (nodeHeight / 2),
  };
  const firstQrt = {
    x: start.x + (end.x - start.x) / 2,
    y: start.y + (end.y - start.y) / 4,
  };
  const firstQrtCtrlPoint = {
    x: start.x + (end.x - start.x) / 2,
    y: start.y,
  };
  const secondQrt = {
    x: start.x + (end.x - start.x) / 2,
    y: start.y + (end.y - start.y) * 3 / 4,
  };
  const secondQrtCtrlPoint = {
    x: start.x + (end.x - start.x) / 2,
    y: end.y,
  };
  return `M ${start.x},${start.y} ` +
    `Q ${firstQrtCtrlPoint.x},${firstQrtCtrlPoint.y},${firstQrt.x},${firstQrt.y} ` +
    `L ${secondQrt.x},${secondQrt.y} ` +
    `Q ${secondQrtCtrlPoint.x},${secondQrtCtrlPoint.y},${end.x},${end.y}`;
}

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
  } = props;

  const ref = useRef() as RefObject<HTMLDivElement>;
  const size = useSize(ref);

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
      g.setEdge(edge.from, edge.to);
    });
    dagre.layout(g);
    return g;
  }, [
    nodes,
    edges,
    nodesep,
    edgesep,
    ranksep,
    rankdir,
    align,
    ranker,
  ]);

  const svgNodes = useMemo(() => {
    let svgElements: React.ReactElement[] = [];
    graph.nodes().forEach((nodeId: string) => {
      const node = graph.node(nodeId) as LineageDagreNode;
      logger.debug('node = %o', node);
      svgElements = svgElements.concat(
        <g
          key={node.id}
          data-node-id={node.id}
        >
          <foreignObject
            x={node.x}
            y={node.y}
            width={node.width || NODE_DEFAULT_WIDTH}
            height={node.height || NODE_DEFAULT_HEIGHT}
          >
            <DatasetNodeCard
              state="default"
              data={node.data}
              useNativeLink
            />
          </foreignObject>
        </g>
      );
    });
    return svgElements;
  }, [
    graph,
  ]);

  const svgEdges = useMemo(() => {
    let svgElements: React.ReactElement[] = [];
    graph.edges().forEach((edgeMeta: dagre.Edge) => {
      const edge = graph.edge(edgeMeta);
      logger.debug('edgeMeta = %o, path = %o', edgeMeta, edge.points);
      svgElements = svgElements.concat(
        <g key={`${edgeMeta.v}-${edgeMeta.w}`}>
          <path
            fill="transparent"
            stroke="#d8d8d8"
            strokeWidth="2"
            d={buildLineageEdgePath(edge.points)}
          />
        </g>
      );
    });
    return svgElements;
  }, [
    graph
  ]);

  return (
    <div className="lineage-board" ref={ref}>
      <KunSpin spinning={loading}>
        <svg width={size.width || 0} height={size.height || 0}>
          {svgEdges}
          {svgNodes}
        </svg>
      </KunSpin>
    </div>
  );
});
