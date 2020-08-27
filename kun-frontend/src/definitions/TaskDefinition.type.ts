import * as T from 'runtypes';

import { TaskDatasetProperty } from '@/definitions/DatasetTaskDefSummary.type';
import { DeployStatusEnum, RunStatusEnum } from '@/definitions/StatEnums.type';

export interface TaskDefinition {
  creator: number | string;
  id: number | string;
  isArchived: boolean;
  isDeployed: boolean;
  name: string;
  taskTemplateName: string;
  taskPayload: TaskPayload | null;
  upstreamTaskDefinitions: { id: string; name: string }[];
  lastUpdateTime: string;
  lastModifier: string;
  createTime: string;
  owner: string | number;
}

export const TaskDefinitionType = T.Record({
  creator: T.String.Or(T.Number),
  id: T.String.Or(T.Number),
  isArchived: T.Boolean,
  isDeployed: T.Boolean,
  name: T.String,
  taskTemplateName: T.String,
  taskPayload: T.Unknown.Or(T.Null),
  upstreamTaskDefinitions: T.Array(T.Record({ id: T.String, name: T.String })),
  lastUpdateTime: T.String.Or(T.Null),
  lastModifier: T.String.Or(T.Null),
  createTime: T.String.Or(T.Null),
  owner: T.String.Or(T.Number),
});

export interface TaskPayload {
  scheduleConfig: ScheduleConfig;
  taskConfig: Record<string, any>;
}

export interface ScheduleConfig {
  cronExpr: string;
  inputNodes: number[];
  type: string;
  inputDatasets: TaskDatasetProperty[];
  outputDatasets: TaskDatasetProperty[];
}

export interface TaskTryVO {
  creator: number;
  definitionId: string | number;
  id: string;
  status: RunStatusEnum;
  taskConfig: Record<string, any>;
  workflowTaskId: string | number;
  workflowTaskRunId: string | number;
}

export interface DeployVO {
  creator: number;
  deployedAt: string;  // Datetime
  deployer: number;
  id: string | number;
  name: string;
  status: DeployStatusEnum;
  submittedAt: string;  // Datetime
}
