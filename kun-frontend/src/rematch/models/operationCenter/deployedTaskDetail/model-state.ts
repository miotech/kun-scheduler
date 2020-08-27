import { DeployedTask } from '@/definitions/DeployedTask.type';
import { TaskRun } from '@/definitions/TaskRun.type';
import { Moment } from 'moment';
import { RunStatusEnum } from '@/definitions/StatEnums.type';

export interface TaskRunListFilter {
  pageNum: number;
  pageSize: number;
  startTime: Moment | null;
  endTime: Moment | null;
  status: RunStatusEnum | null;
}

export interface DeployedTaskDetailModelState {
  deployedTaskId: string | null;
  deployedTask: DeployedTask | null;
  taskRuns: TaskRun[];
  taskRunsCount: number;
  filters: TaskRunListFilter;
}


export const initState: DeployedTaskDetailModelState = {
  filters: {
    pageNum: 1,
    pageSize: 25,
    startTime: null,
    endTime: null,
    status: null,
  },
  deployedTaskId: null,
  deployedTask: null,
  taskRuns: [],
  taskRunsCount: 0,
};
