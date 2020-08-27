import { ScheduleConfig } from '@/definitions/TaskDefinition.type';
import { Tag } from '@/definitions/Tag.type';

export interface Task {
  arguments: TaskParam[];
  dependencies: TaskDependency[];
  description: string;
  id: string;
  name: string;
  operatorId: string;
  scheduleConf: ScheduleConfig;
  tags: Tag[];
  variableDefs: TaskVariable[];
}

export interface TaskParam {
  description: string;
  name: string;
  value: string;
}

export interface TaskVariable {
  defaultValue: string;
  key: string;
  value: string;
}

export interface TaskDependency {
  dependencyFunc: string;
  upstreamTaskId: number;
}
