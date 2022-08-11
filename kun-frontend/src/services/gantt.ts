import { Tasks, QueryGanttTasksParams, WaitTask } from '@/definitions/Gantt.type';
import { ServiceRespPromise } from '@/definitions/common-types';
import { API_DATA_PLATFORM_PREFIX } from '@/constants/api-prefixes';
import { get } from '@/utils/requestUtils';

export async function queryGanttTasks(params: QueryGanttTasksParams): ServiceRespPromise<Tasks> {
  return get('/deployed-taskruns/gantt', {
    query: params,
    prefix: API_DATA_PLATFORM_PREFIX,
  });
}

export async function queryWaitTasks(id: string): ServiceRespPromise<WaitTask[]> {
  return get('/deployed-taskruns/gantt/:id/wait-for', {
    pathParams: { id },
    prefix: API_DATA_PLATFORM_PREFIX,
  });
}
