import { RootDispatch } from '@/rematch/store';
import {
  fetchDeployedTaskDetail,
  fetchTaskRunsOfDeployedTask,
} from '@/services/task-deployments/deployed-tasks';
import { TaskRunListFilter } from '@/rematch/models/operationCenter/deployedTaskDetail/model-state';
import moment from 'moment';

export const effects = (dispatch: RootDispatch) => ({
  async loadDeployedTaskDetailById(id: string) {
    try {
      const taskDetail = await fetchDeployedTaskDetail(id);
      if (taskDetail) {
        dispatch.deployedTaskDetail.setDeployedTask(taskDetail);
      }
    } catch (e) {
      dispatch.deployedTaskDetail.setDeployedTask(null);
    }
  },
  async loadTaskRuns(payload: { id: string } & TaskRunListFilter) {
    try {
      const respData = await fetchTaskRunsOfDeployedTask({
        id: payload.id,
        pageSize: payload.pageSize || 25,
        // @ts-ignore
        pageNum: payload.pageNum || 1,
        status: payload.status || undefined,
        startTime: payload.startTime
          ? moment(payload.startTime).toISOString()
          : undefined,
        endTime: payload.endTime
          ? moment(payload.endTime).toISOString()
          : undefined,
      });
      if (respData && respData.records) {
        dispatch.deployedTaskDetail.setTaskRunsCount(respData.totalCount);
        dispatch.deployedTaskDetail.setTaskRuns(respData.records);
      }
    } catch (e) {}
  },
});
