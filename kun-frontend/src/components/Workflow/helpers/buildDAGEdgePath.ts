// import { TASK_DAG_NODE_WIDTH, TASK_DAG_NODE_HEIGHT } from '../Workflow.constants';

type NodeInfo = {
  srcX: number;
  srcY: number;
  destX: number;
  destY: number;
};

const ARROW_OFFSET = 10;

export type BuildLineageEdgePathOptions = {
  srcOffsetX: number;
  srcOffsetY: number;
  destOffsetX: number;
  destOffsetY: number;
}

export function buildDAGEdgePath({ srcX, srcY, destX, destY }: NodeInfo, options: Partial<BuildLineageEdgePathOptions> = {}): string {
  const {
    srcOffsetX = 0,
    srcOffsetY = 0,
    destOffsetX = 0,
    destOffsetY = 0,
  } = options;

  const start = {
    // x: fromNode.x + nodeWidth + PORT_WIDTH / 2 + srcOffsetX,
    // y: fromNode.y + nodeHeight / 2 + srcOffsetY,
    x: srcX + srcOffsetX,
    y: srcY + srcOffsetY,
  };
  const end = {
    x: destX + destOffsetX,
    y: destY + destOffsetY - ARROW_OFFSET,
  };
  const firstCtrlPoint = {
    x: start.x,
    y: start.y + (end.y - start.y) / 2,
  };
  const secondCtrlPoint = {
    x: end.x,
    y: start.y + (end.y - start.y) / 2 ,
  };
  return `M ${start.x},${start.y} ` +
    `C ${firstCtrlPoint.x},${firstCtrlPoint.y},${secondCtrlPoint.x},${secondCtrlPoint.y},${end.x},${end.y}`;
}
