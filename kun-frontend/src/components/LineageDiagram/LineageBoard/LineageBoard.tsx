import React, { memo, RefObject, useCallback, useMemo, useRef } from 'react';
import { useSize } from 'ahooks';
import dagre from 'dagre';
import { NodeGroup } from 'react-move';
import { DatasetNodeCard, PortStateType } from '@/components/LineageDiagram/DatasetNodeCard/DatasetNodeCard';
import LogUtils from '@/utils/logUtils';
import { KunSpin } from '@/components/KunSpin';
import { Group } from '@visx/group';
import { quadIn } from '@/utils/animation/ease/quad';

import { NodeGroupElement, ReactMoveTiming } from '@/definitions/ReactMove.type';
import { LineageEdge, LineageNode } from '@/definitions/Lineage.type';
import { Dataset } from '@/definitions/Dataset.type';

import {
  EDGE_SEP_DEFAULT, NODE_DEFAULT_HEIGHT, NODE_DEFAULT_WIDTH, NODE_SEP_DEFAULT, PORT_WIDTH, RANK_SEP_DEFAULT
} from './helpers/constants';

import './LineageBoard.less';

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
  onExpandUpstream?: (datasetId: string) => any;
  onExpandDownstream?: (datasetId: string) => any;
  loadingStateNodeIds?: string[];
}

type Props = OwnProps;

type LineageDagreNodeData = Dataset & {
  expandableUpstream?: boolean;
  expandableDownstream?: boolean;
};

type LineageDagreNode = dagre.Node<{
  id: string;
  data: LineageDagreNodeData;
}>;

type LineageNodeGroupElement = NodeGroupElement<LineageDagreNode, { x: number, y: number, opacity: number } & ReactMoveTiming>;

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

  const nodesData = useMemo(() => {
    return graph.nodes().map((nodeId: string) => (graph.node(nodeId) as LineageDagreNode));
  }, [
    graph,
  ]);

  const renderNodeGroupElement = useCallback((nodeGroupElement: LineageNodeGroupElement) => {
    const { data: node, state } = nodeGroupElement;
    logger.trace('In renderNodeGroupElement, nodeGroupElement =', nodeGroupElement);
    return (
      <g
        key={node.id}
        data-node-id={node.id}
      >
        <foreignObject
          x={state.x}
          y={state.y}
          opacity={state.opacity}
          width={(node.width || NODE_DEFAULT_WIDTH) + PORT_WIDTH}
          height={node.height || NODE_DEFAULT_HEIGHT}
        >
          <div style={{ position: 'relative', left: '10px' }}>
            <DatasetNodeCard
              state="default"
              data={node.data}
              leftPortState={computeNodePortState(node.id, node.data?.expandableUpstream || false, loadingStateNodeIds)}
              rightPortState={computeNodePortState(node.id, node.data?.expandableDownstream || false, loadingStateNodeIds)}
              useNativeLink
            />
          </div>
        </foreignObject>
      </g>
    );
  }, []);

  return (
    <div className="lineage-board" ref={ref}>
      <KunSpin spinning={loading}>
        <svg width={size.width || 0} height={size.height || 0}>
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
                {
                  renderingNodes.map((groupElement) => {
                    // logger.debug('groupElement =', groupElement);
                    return renderNodeGroupElement(groupElement);
                  })
                }
              </Group>
            )}
          </NodeGroup>
        </svg>
      </KunSpin>
    </div>
  );
});

function computeNodePortState(id: string, expandable: boolean, loadingStateNodeIds: string[]): PortStateType {
  if (loadingStateNodeIds.indexOf(id) >= 0) {
    return 'loading';
  }
  if (expandable) {
    return 'collapsed';
  }
  // else
  return 'hidden';
}
