import { get, post } from '@/utils/requestUtils';
import { API_DATA_PLATFORM_PREFIX } from '@/constants/api-prefixes';

import { PaginationRespBody, PaginationReqBody, ServiceRespPromise } from '@/definitions/common-types';
import { RunStatusEnum } from '@/definitions/StatEnums.type';
import { TaskRun, TaskRunLog } from '@/definitions/TaskRun.type';

export interface FetchTaskRunsParams extends Omit<PaginationReqBody, 'pageNumber'> {
  status?: RunStatusEnum;
  startTime?: string;
  endTime?: string;
  pageNum: number;
}

export interface FetchTaskRunLogParams {
  start?: number;
  end?: number;
  attempt?: number;
}

// TODO: add mock code for taskruns services
/**
 * @GET /task-tries/{taskDefId}/taskruns
 * Fetch taskruns list
 * @param query
 * @returns
 */
export const fetchTaskRuns = (
  id: string,
  query: FetchTaskRunsParams,
): ServiceRespPromise<PaginationRespBody<TaskRun>> => {
  return get('/task-tries/:id/taskruns', {
    pathParams: { id },
    query: { ...query },
    prefix: API_DATA_PLATFORM_PREFIX,
    mockCode: 'taskruns.search',
  });
};

/**
 * @GET /task-tries/taskruns/:taskRunId/log
 * fetch taskrun log
 * @param taskRunId
 * @returns
 */
export const fetchTaskrunLog = (taskRunId: string, query?: FetchTaskRunLogParams): ServiceRespPromise<TaskRunLog> => {
  return get('/task-tries/taskruns/:taskRunId/log', {
    pathParams: { taskRunId },
    query: { ...(query ?? {}) },
    prefix: API_DATA_PLATFORM_PREFIX,
    mockCode: 'taskruns-log.search',
  });
};

/**
 * @POST /task-tries/taskruns/{taskRunId}/restart
 * restart taskRun instance
 * @param taskRunId
 * @returns
 */
export const restartTaskRunInstance = (taskRunId: string): ServiceRespPromise<TaskRun> => {
  return post('/task-tries/taskruns/:taskRunId/restart', {
    pathParams: { taskRunId },
    prefix: API_DATA_PLATFORM_PREFIX,
  });
};

export const stopTaskRunInstance = (taskRunId: string): ServiceRespPromise<TaskRun> => {
  return post('/task-tries/taskruns/:taskRunId/abort', {
    pathParams: { taskRunId },
    prefix: API_DATA_PLATFORM_PREFIX,
  });
};
