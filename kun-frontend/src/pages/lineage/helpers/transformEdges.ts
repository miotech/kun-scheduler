import { Edge } from '@/definitions/Dataset.type';
import { LineageEdge } from '@/definitions/Lineage.type';

export function transformEdges(
  rawEdges: Edge[],
  selectedId?: string | null,
): LineageEdge[] {
  return rawEdges.map(e => {
    return {
      src: e.sourceVertexId,
      dest: e.destVertexId,
      selected: `${e.sourceVertexId}-${e.destVertexId}` === selectedId,
    };
  });
}
