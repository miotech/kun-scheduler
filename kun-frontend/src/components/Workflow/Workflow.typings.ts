export type WorkflowNodeStatus = 'normal' | 'selected';

export interface WorkflowNode {
  id: string;
  x: number;
  y: number;
  width?: number;
  height?: number;
  name?: string;
  status?: WorkflowNodeStatus;
  isDeployed?: boolean;
  taskTemplateName?: string;
}

export interface WorkflowEdge {
  srcNodeId: string;
  destNodeId: string;
  srcX: number;
  srcY: number;
  destX: number;
  destY: number;
}

export interface Transform {
  scaleX: number;
  scaleY: number;
  transformX: number;
  transformY: number;
}
