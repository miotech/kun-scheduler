import { TaskPayload } from '@/definitions/TaskDefinition.type';
import { RunStatusEnum } from '@/definitions/StatEnums.type';
import { TaskRun } from '@/definitions/TaskRun.type';

export interface DeployedTask {
  id: string;
  isArchived: boolean;
  name: string;
  owner: string | number;
  taskPayload: TaskPayload;
  taskTemplateName: string;
  // stats: Stat[];
  // 'latestRun' can be null when deployed task is not executed/
  latestTaskRun: TaskRun | null;
  taskDefinitionId: string;
  workflowTaskId: string;
}

export interface DeployedTaskDetail extends DeployedTask {
  upstreamDefinitionIds: string[];
}

export interface Stat {
  status: RunStatusEnum;
  count: number;
}

export interface DeployedTaskDependency {
  downStreamTaskId: string;
  upstreamTaskId: string;
}

export interface DeployedTaskDAG {
  nodes: DeployedTask[];
  edges: DeployedTaskDependency[];
}
