import { useRouteMatch } from 'umi';
import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useSize } from 'ahooks';
import Card from '@/components/Card/Card';
import useRedux from '@/hooks/useRedux';
import LineageBoard from '@/components/LineageDiagram/LineageBoard';
import {
  LineageDagreNodeData,
  LineageDagreNode,
} from '@/components/LineageDiagram/LineageBoard/LineageBoard';
import BackButton from '@/components/BackButton/BackButton';
import { LineageDirection } from '@/services/lineage';
import { LineageBoardZoomProvider } from '@/components/LineageDiagram/LineageBoard/LineageBoardZoomProvider';
import SideDropCard from './components/SideDropCard/SideDropCard';
import { transformNodes } from './helpers/transformNodes';
import { transformEdges } from './helpers/transformEdges';

import styles from './index.less';

export default function Lineage() {
  const { selector, dispatch } = useRedux(state => state.lineage);

  const match = useRouteMatch<{ datasetId: string }>();
  const [isExpanded, setIsExpanded] = useState(false);
  const [currentType, setCurrentType] = useState<'dataset' | 'task'>('dataset');

  const boardWrapperRef = useRef<any>();
  const { width: boardWidth, height: boardHeight } = useSize(boardWrapperRef);

  useEffect(() => {
    if (match.params.datasetId !== selector.oldDatasetId) {
      dispatch.lineage.updateState({
        key: 'oldDatasetId',
        value: match.params.datasetId,
      });
      dispatch.lineage.updateGraph({ edges: [], vertices: [] });
      dispatch.lineage.fetchInitialLineageGraphInfo(match.params.datasetId);
    }
  }, [dispatch.lineage, match.params.datasetId, selector.oldDatasetId]);

  const nodes = transformNodes(
    selector.graph.vertices,
    selector.selectedNodeId,
  );
  const edges = transformEdges(
    selector.graph.edges,
    `${selector?.selectedEdgeInfo?.sourceNodeId}-${selector?.selectedEdgeInfo?.destNodeId}`,
  );

  const handleClickNode = useCallback(
    (node: LineageDagreNodeData) => {
      setIsExpanded(true);
      setCurrentType('dataset');
      dispatch.lineage.updateState({
        key: 'selectedNodeId',
        value: node.id,
      });
      dispatch.lineage.updateState({
        key: 'selectedEdgeInfo',
        value: null,
      });
    },
    [dispatch.lineage],
  );
  const handleClickEdge = useCallback(
    (edgeInfo: {
      srcNodeId: string;
      destNodeId: string;
      srcNode: LineageDagreNode;
      destNode: LineageDagreNode;
    }) => {
      setIsExpanded(true);
      setCurrentType('task');
      dispatch.lineage.updateState({
        key: 'selectedEdgeInfo',
        value: {
          sourceNodeId: edgeInfo.srcNodeId,
          destNodeId: edgeInfo.destNodeId,
          sourceNodeName: edgeInfo.srcNode.data?.name || '',
          destNodeName: edgeInfo.destNode.data?.name || '',
        },
      });
      dispatch.lineage.updateState({
        key: 'selectedNodeId',
        value: null,
      });
    },
    [dispatch.lineage],
  );

  const handleClickBackground = useCallback(() => {
    setIsExpanded(false);
    dispatch.lineage.batchUpdateState({
      selectedNodeId: null,
      selectedEdgeInfo: null,
    });
  }, [dispatch.lineage]);

  const handleExpandUpstream = useCallback(
    (id: string) => {
      dispatch.lineage.fetchStreamLineageGraphInfo({
        id,
        direction: LineageDirection.UPSTREAM,
      });
    },
    [dispatch.lineage],
  );

  const handleExpandDownstream = useCallback(
    (id: string) => {
      dispatch.lineage.fetchStreamLineageGraphInfo({
        id,
        direction: LineageDirection.DOWNSTREAM,
      });
    },
    [dispatch.lineage],
  );

  return (
    <div className={styles.page}>
      <BackButton
        defaultUrl={`/data-discovery/dataset/${match.params.datasetId}`}
      />

      <Card className={styles.content}>
        <div
          ref={boardWrapperRef as any}
          style={{ position: 'relative', width: '100%', height: '100%' }}
        >
          <LineageBoardZoomProvider
            width={boardWidth || 1000}
            height={boardHeight || 500}
            scaleXMin={0.1}
            scaleYMin={0.1}
            scaleXMax={2}
            scaleYMax={2}
          >
            <LineageBoard
              width={boardWidth || 1000}
              height={(boardHeight || 600) - 10}
              nodes={nodes}
              edges={edges}
              loading={selector.graphLoading}
              onClickNode={handleClickNode}
              onClickEdge={handleClickEdge}
              onClickBackground={handleClickBackground}
              onExpandUpstream={handleExpandUpstream}
              onExpandDownstream={handleExpandDownstream}
            />
          </LineageBoardZoomProvider>
          <SideDropCard
            isExpanded={isExpanded}
            datasetId={selector.selectedNodeId}
            sourceDatasetId={selector.selectedEdgeInfo?.sourceNodeId}
            destDatasetId={selector.selectedEdgeInfo?.destNodeId}
            sourceDatasetName={selector.selectedEdgeInfo?.sourceNodeName}
            destDatasetName={selector.selectedEdgeInfo?.destNodeName}
            onExpand={(v: boolean) => setIsExpanded(v)}
            type={currentType}
          />
        </div>
      </Card>
    </div>
  );
}
