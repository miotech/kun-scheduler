import { DeployedTask } from '@/definitions/DeployedTask.type';

export interface ScheduledTasksFilterState {
  searchName: string;
  owners: string[];
  pageNum: number;
  pageSize: number;
  taskTemplateName: string;
}

export interface ScheduledTasksModelState {
  filters: ScheduledTasksFilterState;
  deployedTasksTableData: DeployedTask[];
  totalCount: number;
  shouldRefresh: boolean;
}

export const defaultFilter: ScheduledTasksFilterState = {
  searchName: '',
  owners: [],
  pageNum: 1,
  pageSize: 25,
  taskTemplateName: '',
};

export const initState: ScheduledTasksModelState = {
  filters: {
    ...defaultFilter,
  },
  deployedTasksTableData: [],
  totalCount: 0,
  shouldRefresh: true,
};
