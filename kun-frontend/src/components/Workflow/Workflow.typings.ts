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
}
