import {
  NODE_DEFAULT_HEIGHT,
  NODE_DEFAULT_WIDTH,
  PORT_WIDTH,
} from './constants';

type NodeInfo = {
  x: number;
  y: number;
  width?: number;
  height?: number;
};

const ARROW_X_OFFSET = 10;

export type BuildLineageEdgePathOptions = {
  srcOffsetX: number;
  srcOffsetY: number;
  destOffsetX: number;
  destOffsetY: number;
}

export function buildLineageEdgePath(fromNode: NodeInfo, toNode: NodeInfo, options: Partial<BuildLineageEdgePathOptions> = {}): string {
  const [
    nodeWidth = NODE_DEFAULT_WIDTH,
    nodeHeight = NODE_DEFAULT_HEIGHT,
  ] = [
    fromNode.width,
    toNode.height,
  ];
  const {
    srcOffsetX = 0,
    srcOffsetY = 0,
    destOffsetX = 0,
    destOffsetY = 0,
  } = options;

  const start = {
    x: fromNode.x + nodeWidth + PORT_WIDTH / 2 + srcOffsetX,
    y: fromNode.y + nodeHeight / 2 + srcOffsetY,
  };
  const end = {
    x: toNode.x + PORT_WIDTH / 2 - ARROW_X_OFFSET + destOffsetX,
    y: toNode.y + nodeHeight / 2 + destOffsetY,
  };
  const firstCtrlPoint = {
    x: start.x + (end.x - start.x) / 2,
    y: start.y,
  };
  const secondCtrlPoint = {
    x: start.x + (end.x - start.x) / 2,
    y: end.y,
  };
  return `M ${start.x},${start.y} ` +
    `C ${firstCtrlPoint.x},${firstCtrlPoint.y},${secondCtrlPoint.x},${secondCtrlPoint.y},${end.x},${end.y}`;
}
