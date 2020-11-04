import { Vertex } from '@/definitions/Dataset.type';
import { LineageNode } from '@/definitions/Lineage.type';

export function transformNodes(
  basicNodes: Vertex[],
  selectedId?: string | null,
): LineageNode[] {
  return basicNodes.map(n => {
    return {
      id: n.vertexId,
      data: {
        id: n.datasetBasic.gid,
        name: n.datasetBasic.name,
        datasource: n.datasetBasic.datasource,
        type: n.datasetBasic.type,
        rowCount: n.datasetBasic.rowCount,
        highWatermark: n.datasetBasic.highWatermark,
        selected: n.vertexId === selectedId,
        expandableUpstream: n.upstreamVertexCount > 0,
        expandableDownstream: n.downstreamVertexCount > 0,
        inDegree: n.upstreamVertexCount,
        outDegree: n.downstreamVertexCount,
      },
    };
  });
}
