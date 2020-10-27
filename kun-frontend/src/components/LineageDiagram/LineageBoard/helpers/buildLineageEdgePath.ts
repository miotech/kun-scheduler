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

export function buildLineageEdgePath(fromNode: NodeInfo, toNode: NodeInfo): string {
  const [
    nodeWidth = NODE_DEFAULT_WIDTH,
    nodeHeight = NODE_DEFAULT_HEIGHT,
  ] = [
    fromNode.width,
    toNode.height,
  ];
  const start = {
    x: fromNode.x + nodeWidth + PORT_WIDTH / 2,
    y: fromNode.y + nodeHeight / 2,
  };
  const end = {
    x: toNode.x + PORT_WIDTH / 2 - ARROW_X_OFFSET,
    y: toNode.y + nodeHeight / 2,
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
