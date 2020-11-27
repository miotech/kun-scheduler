import dagre from 'dagre';
import isArray from 'lodash/isArray';
import find from 'lodash/find';
import LogUtils from '@/utils/logUtils';

import { TaskDefinition } from '@/definitions/TaskDefinition.type';
import { WorkflowEdge, WorkflowNode } from '@/components/Workflow/Workflow.typings';
// import uniqueId from 'lodash/uniqueId';


const logger = LogUtils.getLoggers('convertTaskDefinitionsToGraph');

const DEFAULT_WIDTH = 240;
const DEFAULT_HEIGHT = 90;

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
  graph.setGraph({});
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
        graph.setEdge(
          `${upstreamTask.id}`,
          `${taskDef.id}`,
          {}
        );
      });
    }
  });

  dagre.layout(graph, {
    rankdir: 'TB',  // Top-to-bottom
    nodesep: DEFAULT_WIDTH * 6.0,
    ranksep: DEFAULT_HEIGHT * 6.0,
    // ranker: 'tight-tree',
  });

  let graphWidth = 0;
  let graphHeight = 0;

  const nodes: WorkflowNode[] = graph.nodes().map(nodeId => {
    const taskDef = find(taskDefinitions, t => `${t.id}` === nodeId);
    const n = graph.node(nodeId);
    graphWidth = Math.max(graphWidth, n.x + DEFAULT_WIDTH);
    graphHeight = Math.max(graphHeight, n.y + DEFAULT_HEIGHT);
    return {
      id: nodeId,
      name: taskDef?.name || '',
      x: n.x,
      y: n.y,
      width: DEFAULT_WIDTH,
      height: DEFAULT_HEIGHT,
      isDeployed: taskDef?.isDeployed || false,
      status: 'normal',
      taskTemplateName: taskDef?.taskTemplateName || '',
    };
  });

  const edges: WorkflowEdge[] = [];
  graph.edges().forEach(edgeInfo => {
    const edge = graph.edge(edgeInfo);
    edges.push({
      srcNodeId: `${edgeInfo.v}`,
      destNodeId: `${edgeInfo.w}`,
      srcX: edge.points[0].x,
      srcY: edge.points[0].y,
      destX: edge.points[edge.points.length - 1].x,
      destY: edge.points[edge.points.length - 1].y,
    });
  });

  return {
    nodes,
    edges,
    graphWidth,
    graphHeight,
  };
}
