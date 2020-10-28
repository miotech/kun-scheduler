import { Graph } from '@/rematch/models/lineage';
import uniqBy from 'lodash/uniqBy';

export function mergeGraph(oldGraph: Graph, newGraph: Graph) {
  const { vertices: oldVertices, edges: oldEdges } = oldGraph;
  const { vertices: newVertices, edges: newEdges } = newGraph;
  const vertices = uniqBy([...oldVertices, ...newVertices], 'vertexId');
  const edges = uniqBy(
    [...oldEdges, ...newEdges],
    i => `${i.sourceVertexId}-${i.destVertexId}`,
  );
  return { vertices, edges };
}
