import { graphlib } from 'dagre';

type Graph = graphlib.Graph<any>;

/**
 * Given a graph and a source node id, collect all its upstream nodes and return
 * @param graph
 * @param srcNodeId
 */
export function collectUpstreamNodes(graph: Graph, srcNodeId: string): string[] {
  const srcNode = graph.node(srcNodeId);
  if (!srcNode) {
    return [];
  }
  const searchQueue: string[] = [srcNode.id];
  const collectionSet = new Set<string>();
  while (searchQueue.length) {
    const currentNode = graph.node(searchQueue.shift() as string);
    if (currentNode) {
      collectionSet.add(currentNode.id);
      const inEdges = graph.inEdges(currentNode.id);
      // eslint-disable-next-line no-loop-func
      inEdges?.forEach(edge => {
        if (!collectionSet.has(edge.v)) {
          searchQueue.push(edge.v);
        }
      });
    }
  }
  collectionSet.delete(srcNode.id);
  return Array.from(collectionSet);
}

/**
 * Given a graph and a source node id, collect all its downstream nodes and return
 * @param graph
 * @param srcNodeId
 */
export function collectDownstreamNodes(graph: Graph, srcNodeId: string): string[] {
  const srcNode = graph.node(srcNodeId);
  if (!srcNode) {
    return [];
  }
  const searchQueue = [srcNode.id];
  const collectionSet = new Set<string>();
  while (searchQueue.length) {
    const currentNode = graph.node(searchQueue.shift() as string);
    if (currentNode) {
      collectionSet.add(currentNode.id);
      const outEdges = graph.outEdges(currentNode.id);
      // eslint-disable-next-line no-loop-func
      outEdges?.forEach(edge => {
        if (!collectionSet.has(edge.w)) {
          searchQueue.push(edge.w);
        }
      });
    }
  }
  collectionSet.delete(srcNode.id);
  return Array.from(collectionSet);
}
