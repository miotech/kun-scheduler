import {
  initState, DeployedTaskDetailModelState as ModelState, TaskRunListFilter
} from './model-state';
import produce from 'immer';
import { DeployedTask } from '@/definitions/DeployedTask.type';
import { TaskRun } from '@/definitions/TaskRun.type';

export const reducers = {
  resetAll: (): ModelState => ({
    ...initState,
  }),
  setDeployedTaskId: produce((draftState: ModelState, payload: string | null) => {
    draftState.deployedTaskId = payload;
  }),
  setDeployedTask: produce((draftState: ModelState, payload: DeployedTask | null) => {
    draftState.deployedTask = payload;
  }),
  setTaskRuns: produce((draftState: ModelState, payload: TaskRun[]) => {
    draftState.taskRuns = payload;
  }),
  setTaskRunsCount: produce((draftState: ModelState, payload: number) => {
    draftState.taskRunsCount = payload;
  }),
  updateFilter: produce((draftState: ModelState, payload: Partial<TaskRunListFilter>) => {
    draftState.filters = {
      ...draftState.filters,
      ...payload,
    };
  }),
};
