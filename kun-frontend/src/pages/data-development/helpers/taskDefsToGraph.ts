import dagre from 'dagre';
import isArray from 'lodash/isArray';
import find from 'lodash/find';
import LogUtils from '@/utils/logUtils';

import { TaskDefinition } from '@/definitions/TaskDefinition.type';
import { WorkflowEdge, WorkflowNode } from '@/components/Workflow/Workflow.typings';
// import uniqueId from 'lodash/uniqueId';


const logger = LogUtils.getLoggers('convertTaskDefinitionsToGraph');

const DEFAULT_WIDTH = 220;
const DEFAULT_HEIGHT = 60;

export function convertTaskDefinitionsToGraph(taskDefinitions: TaskDefinition[]): {
  nodes: WorkflowNode[];
  edges: WorkflowEdge[];
  graphWidth: number;
  graphHeight: number;
} {
  if (!isArray(taskDefinitions)) {
    logger.warn('Invalid argument taskDefinitions: %o', taskDefinitions);
    return {
      nodes: [],
      edges: [],
      graphWidth: 0,
      graphHeight: 0,
    };
  }

  const graph = new dagre.graphlib.Graph({
    directed: true,
  });
  // Set an object for the graph label
  graph.setGraph({
    nodesep: DEFAULT_WIDTH / 2,
    ranksep: DEFAULT_HEIGHT,
    width: DEFAULT_WIDTH,
    height: DEFAULT_HEIGHT,
  });
  // Default to assigning a new object as a label for each new edge.
  graph.setDefaultEdgeLabel(() => ({}));

  taskDefinitions.forEach(taskDef => {
    // insert node
    graph.setNode(`${taskDef.id}`, {
      id: `${taskDef.id}`,
      name: `${taskDef.name}`,
      width: DEFAULT_WIDTH,
      height: DEFAULT_HEIGHT,
    });
    // insert relations
    if (taskDef.upstreamTaskDefinitions?.length > 0) {
      taskDef.upstreamTaskDefinitions.forEach(upstreamTask => {
        if (graph.node(upstreamTask.id) != null) {
          graph.setEdge(
            `${upstreamTask.id}`,
            `${taskDef.id}`,
            {}
          );
        }
      });
    }
  });

  dagre.layout(graph, {
    rankdir: 'TB',  // Top-to-bottom
    nodesep: DEFAULT_WIDTH / 2,
    ranksep: DEFAULT_HEIGHT,
    // ranker: 'tight-tree',
  });

  let graphWidth = 0;
  let graphHeight = 0;

  const nodes: WorkflowNode[] = [];
  graph.nodes().forEach(nodeId => {
    const taskDef = find(taskDefinitions, t => `${t.id}` === `${nodeId}`);
    const n = graph.node(nodeId);
    if (n != null) {
      graphWidth = Math.max(graphWidth, (n.x || 0) + DEFAULT_WIDTH);
      graphHeight = Math.max(graphHeight, (n.y || 0) + DEFAULT_HEIGHT);
      nodes.push({
        id: nodeId,
        name: taskDef?.name || '',
        x: n.x || 0,
        y: n.y || 0,
        width: DEFAULT_WIDTH,
        height: DEFAULT_HEIGHT,
        isDeployed: taskDef?.isDeployed || false,
        status: 'normal',
        taskTemplateName: taskDef?.taskTemplateName || '',
      });
    }
  });

  const edges: WorkflowEdge[] = [];
  graph.edges().forEach(edgeInfo => {
    // const edge = graph.edge(edgeInfo);
    const srcNode = graph.node(edgeInfo.v);
    const destNode = graph.node(edgeInfo.w);

    if (srcNode != null && destNode != null) {
      edges.push({
        srcNodeId: `${edgeInfo.v}`,
        destNodeId: `${edgeInfo.w}`,
        srcX: srcNode.x + DEFAULT_WIDTH / 2,
        srcY: srcNode.y + DEFAULT_HEIGHT,
        destX: destNode.x + DEFAULT_WIDTH / 2,
        destY: destNode.y,
      });
    }
  });

  return {
    nodes,
    edges,
    graphWidth,
    graphHeight,
  };
}
