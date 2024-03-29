import { RootDispatch } from '@/rematch/store';
import { ScheduledTasksFilterState } from '@/rematch/models/operationCenter/scheduledTasks/model-state';
import * as deployedTaskServices from '@/services/task-deployments/deployed-tasks';

export const effects = (dispatch: RootDispatch) => ({
  async fetchScheduledTasks(payload: ScheduledTasksFilterState) {
    const responseData = await deployedTaskServices.fetchDeployedTasks({
      name: payload.searchName || undefined,
      owners: payload.owners?.length ? payload.owners : undefined,
      scheduledTaskRunsOnly: true,
      // @ts-ignore
      pageNum: payload.pageNum ?? 1,
      pageSize: payload.pageSize ?? 25,
      taskTemplateName: payload.taskTemplateName || undefined,
    });
    if (responseData) {
      dispatch.scheduledTasks.setDeployedTasksTableData(responseData.records);
      dispatch.scheduledTasks.setTotalCount(parseInt(`${responseData.totalCount}`, 10));
    }
    dispatch.scheduledTasks.setShouldRefresh(false);
  },
});
