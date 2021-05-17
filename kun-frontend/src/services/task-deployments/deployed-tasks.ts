import { get, post, put } from '@/utils/requestUtils';
import { API_DATA_PLATFORM_PREFIX } from '@/constants/api-prefixes';

import { PaginationReqBody, PaginationRespBody, ServiceRespPromise } from '@/definitions/common-types';
import { TaskRun, TaskRunDAG, TaskRunLog } from '@/definitions/TaskRun.type';
import { RunStatusEnum } from '@/definitions/StatEnums.type';
import { DeployedTask, DeployedTaskDAG, DeployedTaskDetail } from '@/definitions/DeployedTask.type';
import axios from 'axios';
import SafeUrlAssembler from 'safe-url-assembler';

/**
 * GET /deploy-taskruns
 * Search scheduled taskruns
 */

export interface FetchScheduledTaskRunsReqParams extends Partial<PaginationReqBody> {
  name?: string;
  definitionsIds?: string[];
  ownerId?: (number | string)[];
  status?: RunStatusEnum;
  taskTemplateName?: string;
}

export async function fetchScheduledTaskRuns(
  reqParams: FetchScheduledTaskRunsReqParams = {},
): ServiceRespPromise<PaginationRespBody<TaskRun>> {
  return get('/deployed-taskruns', {
    query: { ...reqParams },
    prefix: API_DATA_PLATFORM_PREFIX,
    mockCode: 'deployed-taskruns.search',
  });
}

/**
 * GET /deployed-taskruns/{id}
 * Get detail of scheduled taskrun
 */

export async function fetchDetailedScheduledTaskRun(taskRunId: string): ServiceRespPromise<TaskRun> {
  return get('/deployed-taskruns/:id', {
    pathParams: { id: taskRunId },
    prefix: API_DATA_PLATFORM_PREFIX,
  });
}

/**
 * GET /deployed-taskruns/{id}/log
 * Get log of scheduled taskrun
 */

export async function fetchScheduledTaskRunLog(taskRunId: string): ServiceRespPromise<TaskRunLog> {
  return get('/deployed-taskruns/:id/log', {
    pathParams: { id: taskRunId },
    prefix: API_DATA_PLATFORM_PREFIX,
  });
}

export async function fetchScheduledTaskRunLogWithoutErrorNotification(
  taskRunId: string,
  attempt: number = -1,
  startLineIndex: number | undefined = -5000,
): ServiceRespPromise<TaskRunLog> {
  const axiosInstance = axios.create();
  return axiosInstance
    .get(
      SafeUrlAssembler()
        .template('/deployed-taskruns/:id/log')
        .prefix(API_DATA_PLATFORM_PREFIX)
        .param({
          id: taskRunId,
          start: startLineIndex,
          ...(attempt > 0 ? { attempt } : {}),
        })
        .toString(),
    )
    .then(response => response.data?.result);
}

/**
 * GET /deployed-tasks
 * Search deployed tasks
 */

export interface FetchDeployedTasksReqParams extends Partial<PaginationReqBody> {
  name?: string;
  definitionIds?: string[];
  ownerId?: (number | string)[];
  status?: RunStatusEnum;
  taskTemplateName?: string;
  workflowTaskIds?: string[];
}

export async function fetchDeployedTasks(
  reqParams: FetchDeployedTasksReqParams,
): ServiceRespPromise<PaginationRespBody<DeployedTask>> {
  return get('/deployed-tasks', {
    query: {
      ...reqParams,
      definitionIds: reqParams?.definitionIds?.length ? reqParams.definitionIds?.join(',') : reqParams.definitionIds,
    },
    prefix: API_DATA_PLATFORM_PREFIX,
    mockCode: 'deployed-tasks.search',
  });
}

export async function getTaskDefinitionIdByWorkflowTaskId(workflowTaskId: string): Promise<string | null> {
  return get<PaginationRespBody<DeployedTask>>('/deployed-tasks', {
    query: {
      workflowTaskIds: [workflowTaskId],
    },
    prefix: API_DATA_PLATFORM_PREFIX,
    mockCode: 'deployed-tasks.search',
  }).then(data => {
    if (data.records?.length > 0) {
      return data.records[0].taskDefinitionId;
    }
    // else
    return null;
  });
}

export async function getTaskDefinitionIdByWorkflowIds(
  workflowTaskIds: string[],
): Promise<Record<string, string | null>> {
  if (!workflowTaskIds || !workflowTaskIds.length) {
    return {};
  }
  return get<PaginationRespBody<DeployedTask>>('/deployed-tasks', {
    query: {
      workflowTaskIds: [...workflowTaskIds],
    },
    prefix: API_DATA_PLATFORM_PREFIX,
    mockCode: 'deployed-tasks.search',
  }).then(data => {
    const ret: Record<string, string | null> = {};
    workflowTaskIds.forEach(workflowTaskId => {
      const item = data.records.find(record => record.workflowTaskId === workflowTaskId);
      if (item) {
        ret[workflowTaskId] = item.taskDefinitionId;
      } else {
        ret[workflowTaskId] = null;
      }
    });
    return ret;
  });
}

/**
 * GET /deployed-tasks/{id}
 * Get deployed task
 */

export async function fetchDeployedTaskDetail(deployedTaskId: string): ServiceRespPromise<DeployedTaskDetail> {
  return get('/deployed-tasks/:id', {
    pathParams: { id: deployedTaskId },
    prefix: API_DATA_PLATFORM_PREFIX,
    mockCode: 'deployed-tasks.get-detail',
  });
}

/**
 * GET /deployed-tasks/{id}/dag
 * Get dag of deployed task
 */

export interface FetchDeployedTaskDAGOptionParams {
  upstreamLevel?: number;
  downstreamLevel?: number;
}

export async function fetchDeployedTaskDAG(
  deployedTaskId: string,
  optionParams: FetchDeployedTaskDAGOptionParams = {},
): ServiceRespPromise<DeployedTaskDAG> {
  return get('/deployed-tasks/:id/dag', {
    query: { ...optionParams },
    pathParams: {
      id: deployedTaskId,
    },
    prefix: API_DATA_PLATFORM_PREFIX,
    mockCode: 'deployed-tasks.get-dag',
  });
}

/**
 * GET /deployed-tasks/{id}/taskruns
 * Search task runs of deployed task
 */

export type ScheduleTypeEnum = 'SCHEDULED' | 'NONE' | 'MANUAL';

export interface FetchTaskRunsOfDeployedTaskReqParams extends Partial<PaginationReqBody> {
  id: string;
  startTime?: string;
  endTime?: string;
  status?: RunStatusEnum;
  scheduleTypes?: ScheduleTypeEnum[];
}

export async function fetchTaskRunsOfDeployedTask(
  reqParams: FetchTaskRunsOfDeployedTaskReqParams,
): ServiceRespPromise<PaginationRespBody<TaskRun>> {
  const { id, ...restParams } = reqParams;
  return get('/deployed-tasks/:id/taskruns', {
    pathParams: { id },
    query: { ...restParams },
    prefix: API_DATA_PLATFORM_PREFIX,
    mockCode: 'deployed-tasks.get-task-runs',
  });
}

/**
 * GET /deployed-taskruns/{id}/dag
 * Get dag of deployed task
 */

export interface FetchTaskRunDAGOptionParams {
  upstreamLevel?: number;
  downstreamLevel?: number;
}

export async function fetchTaskRunDAG(
  taskRunId: string,
  optionParams: FetchTaskRunDAGOptionParams = {},
): ServiceRespPromise<TaskRunDAG> {
  return get('/deployed-taskruns/:id/dag', {
    query: { ...optionParams },
    pathParams: {
      id: taskRunId,
    },
    prefix: API_DATA_PLATFORM_PREFIX,
    mockCode: 'deployed-tasks.get-taskrun-dag',
  });
}

/**
 * Restart a deployed task run instance
 * @param taskRunId
 */
export async function restartTaskRunInstance(taskRunId: string): ServiceRespPromise<TaskRun> {
  return post('/deployed-taskruns/:taskRunId/_restart', {
    pathParams: {
      taskRunId,
    },
    prefix: API_DATA_PLATFORM_PREFIX,
  });
}

/**
 * Abort a deployed task run instance
 * @param taskRunId
 */
export async function abortTaskRunInstance(taskRunId: string): ServiceRespPromise<TaskRun> {
  return put('/deployed-taskruns/:taskRunId/_abort', {
    pathParams: {
      taskRunId,
    },
    prefix: API_DATA_PLATFORM_PREFIX,
  });
}
