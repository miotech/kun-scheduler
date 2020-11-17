import { PaginationRespBody, ServiceRespPromise } from '@/definitions/common-types';
import { DeployVO, TaskDefinition, TaskPayload, TaskTryVO } from '@/definitions/TaskDefinition.type';
import { TaskRunLog } from '@/definitions/TaskRun.type';

import { delet, get, post, put } from '@/utils/requestUtils';
import { API_DATA_PLATFORM_PREFIX } from '@/constants/api-prefixes';
import { TaskDefinitionViewVO } from '@/definitions/TaskDefinitionView.type';

/**
 * Get all task definitions (Used by DAG)
 */
export async function fetchAllTaskDefinitions(): ServiceRespPromise<TaskDefinition[]> {
  return get('/task-definitions', {
    prefix: API_DATA_PLATFORM_PREFIX,
    mockCode: 'task-definitions.fetchAll',
  });
}

/**
 * Search task definitions
 * GET /api/task-definitions
 */

export interface SearchTaskDefinitionReqParams {
  archived?: boolean;
  creatorIds?: number[];
  definitionIds?: number[];
  name?: string;
  pageNum?: number;
  pageSize?: number;
  taskTemplateName?: string;
}

export async function searchTaskDefinition(reqParams: SearchTaskDefinitionReqParams): ServiceRespPromise<PaginationRespBody<TaskDefinition>> {
  return get('/task-definitions/_search', {
    query: {
      // by default, we filter out archived
      archived: false,
      pageNum: 1,
      pageSize: 25,
      ...reqParams,
    },
    prefix: API_DATA_PLATFORM_PREFIX,
    mockCode: 'task-definitions.search',
  });
}

/**
 * Create Task Definition
 * POST /api/task-definitions
 */

export interface CreateTaskDefinitionReqParams {
  name: string;
  taskTemplateName: string;
}

export async function createTaskDefinition(reqParams: CreateTaskDefinitionReqParams): ServiceRespPromise<TaskDefinition> {
  return post('/task-definitions', {
    data: { ...reqParams },
    prefix: API_DATA_PLATFORM_PREFIX,
    mockCode: 'task-definitions.create',
  });
}

/**
 * Fetch Task Definition Details
 * GET /api/task-definitions/{id}
 */

export async function fetchTaskDefinitionDetail(id: string | number): ServiceRespPromise<TaskDefinition> {
  return get('/task-definitions/:id', {
    pathParams: {
      id: `${id}`,
    },
    prefix: API_DATA_PLATFORM_PREFIX,
    mockCode: 'task-definitions.get-detail',
  });
}

/**
 * Update Task Definition
 * PUT /task-definitions/{id}
 */

export interface UpdateTaskDefinitionReqParams {
  id: number | string;
  taskPayload: TaskPayload;
  name: string;
  owner: string | number;
}

export async function updateTaskDefinition(reqParams: UpdateTaskDefinitionReqParams): ServiceRespPromise<TaskDefinition> {
  const {
    id,
    ...restReqParams
  } = reqParams;
  return put('/task-definitions/:id', {
    pathParams: { id: `${id}` },
    data: {
      definitionId: id,
      ...restReqParams,
    },
    prefix: API_DATA_PLATFORM_PREFIX,
    mockCode: 'task-definitions.update',
  });
}

/**
 * Delete Task Definition
 * DELETE /task-definitions/{id}
 */

export async function deleteTaskDefinition(id: string | number): ServiceRespPromise<{}> {
  return delet('/task-definitions/:id', {
    pathParams: {
      id: `${id}`,
    },
    prefix: API_DATA_PLATFORM_PREFIX,
    mockCode: 'task-definitions.delete',
  });
}

/**
 * Dry run task definition
 * POST /task-definitions/{id}/_run
 */

export interface DryRunTaskDefReqParams {
  taskDefId: string | number;
  parameters: Record<string, any>;
  variables: Record<string, any>;
}

export async function dryRunTaskDefinition(reqParams: DryRunTaskDefReqParams): ServiceRespPromise<TaskTryVO> {
  return post('/task-definitions/:id/_run', {
    pathParams: {
      id: reqParams.taskDefId,
    },
    data: {
      parameters: reqParams.parameters,
      variables: reqParams.variables,
    },
    prefix: API_DATA_PLATFORM_PREFIX,
    mockCode: 'task-definitions.run'
  });
}

/**
 * Check if config of current task definition is valid
 * POST /task-definitions/{id}/_check
 */

export async function checkTaskDefinitionConfigIsValid(taskDefId: string | number): ServiceRespPromise<TaskTryVO> {
  return post('/task-definitions/:id/_check', {
    pathParams: { id: taskDefId },
    prefix: API_DATA_PLATFORM_PREFIX,
    mockCode: 'task-definitions.check'
  });
}

/**
 * Commit and deploy a Task definition
 * POST /task-definitions/{id}/_deploy
 */

export async function commitAndDeployTaskDefinition(taskDefId: string | number, message: string): ServiceRespPromise<DeployVO> {
  return post('/task-definitions/:id/_deploy', {
    pathParams: { id: taskDefId },
    data: {
      definitionId: taskDefId,
      message,
    },
    prefix: API_DATA_PLATFORM_PREFIX,
    mockCode: 'task-definitions.deploy'
  });
}

/**
 * Get task try
 * GET /task-tries/{id}
 */

export async function fetchTaskTry(taskTryId: string | number): ServiceRespPromise<TaskTryVO> {
  return get('/task-tries/:id', {
    pathParams: { id: taskTryId },
    prefix: API_DATA_PLATFORM_PREFIX,
    mockCode: 'task-tries.get',
  });
}

/**
 * Stop task try
 * POST /task-tries/{id}
 */

export async function stopTaskTry(taskTryId: string | number): ServiceRespPromise<TaskTryVO> {
  return post('/task-tries/:id/_stop', {
    pathParams: { id: taskTryId },
    prefix: API_DATA_PLATFORM_PREFIX,
    mockCode: 'task-tries.stop',
  });
}

/**
 * Get task try log
 * GET /task-tries/{id}/log
 */

export interface FetchTaskTryLogExtraParams {
  start?: number | string;
  end?: number | string;
}

export async function fetchTaskTryLog(taskTryId: string | number, filters: FetchTaskTryLogExtraParams = {}): ServiceRespPromise<TaskRunLog> {
  return get('/task-tries/:id/log', {
    pathParams: { id: taskTryId },
    query: filters,
    prefix: API_DATA_PLATFORM_PREFIX,
    mockCode: 'task-tries.get-log',
  });
}

/**
 * Given a task definition, return all task definition views that contains it.
 * @param taskDefId
 */
export async function fetchTaskDefViewsByTaskDefId(taskDefId: string) {
  return get<TaskDefinitionViewVO[]>('/task-definitions/:taskDefId/task-def-views', {
    prefix: API_DATA_PLATFORM_PREFIX,
    pathParams: {
      taskDefId,
    },
    mockCode: 'task-definitions.get-views-by-def-id',
  });
}
