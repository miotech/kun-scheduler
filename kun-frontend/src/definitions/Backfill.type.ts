import { TaskRun } from '@/definitions/TaskRun.type';

export type Backfill = {
  id: string;
  name: string;
  taskRunIds: string[];
  taskDefinitionIds: string[];
  workflowTaskIds: string[];
  createTime: string;
  updateTime: string;
  creator: string;
};

export type BackfillDetail = Backfill & {
  taskRunList: TaskRun[];
};
