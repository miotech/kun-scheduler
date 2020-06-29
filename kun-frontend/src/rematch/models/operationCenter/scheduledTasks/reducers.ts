import {
  defaultFilter, initState, ScheduledTasksFilterState, ScheduledTasksModelState as ModelState
} from './model-state';
import { DeployedTask } from '@/definitions/DeployedTask.type';

export const reducers = {
  resetAll: (): ModelState => ({
    ...initState,
  }),
  resetFilter: (state: ModelState): ModelState => ({
    ...state,
    filters: {
      ...defaultFilter,
    },
  }),
  updateFilter: (state: ModelState, payload: Partial<ScheduledTasksFilterState>): ModelState => ({
    ...state,
    filters: {
      ...state.filters,
      ...payload,
    },
  }),
  setShouldRefresh: (state: ModelState, payload: boolean): ModelState => ({
    ...state,
    shouldRefresh: payload,
  }),
  setDeployedTasksTableData: (state: ModelState, payload: DeployedTask[]): ModelState => ({
    ...state,
    deployedTasksTableData: payload,
  }),
  setTotalCount:  (state: ModelState, payload: number): ModelState => ({
    ...state,
    totalCount: payload,
  }),
};
