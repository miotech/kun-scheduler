import { delet, get, patch, post, put } from '@/utils/requestUtils';
import { API_DATA_PLATFORM_PREFIX } from '@/constants/api-prefixes';
import {
  SearchTaskDefinitionViewParams, TaskDefinitionView, TaskDefinitionViewUpdateVO, TaskDefinitionViewVO
} from '@/definitions/TaskDefinitionView.type';
import { PaginationRespBody } from '@/definitions/common-types';
import { TaskDefinition } from '@/definitions/TaskDefinition.type';

/**
 * Search task definition views
 * @param searchParams
 */
export async function searchTaskDefinitionViews(searchParams: SearchTaskDefinitionViewParams) {
  return get<PaginationRespBody<TaskDefinitionViewVO>>('/task-def-views', {
    prefix: API_DATA_PLATFORM_PREFIX,
    query: {
      keyword: searchParams.keyword,
      pageNum: searchParams.pageNumber,
      pageSize: searchParams.pageSize,
      sortKey: searchParams.sortColumn,
      sortOrder: searchParams.sortOrder,
    },
    mockCode: 'task-def-views.search',
  });
}

export async function fetchTaskDefinitionViewDetail(viewId: string) {
  return get<TaskDefinitionView>('/task-def-views/:viewId', {
    prefix: API_DATA_PLATFORM_PREFIX,
    pathParams: {
      viewId,
    },
    mockCode: 'task-def-views.getDetail',
  });
}

/**
 * Create a task definition view
 * @param createParams
 */
export async function createTaskDefinitionView(createParams: TaskDefinitionViewUpdateVO) {
  return post<TaskDefinitionView>('/task-def-views', {
    prefix: API_DATA_PLATFORM_PREFIX,
    data: createParams,
    mockCode: 'task-def-views.create',
  });
}

/**
 * Update a task definition view
 * @param viewId
 * @param updateParams
 */
export async function updateTaskDefinitionView(viewId: string, updateParams: TaskDefinitionViewUpdateVO) {
  return put<TaskDefinitionView>('/task-def-views/:viewId', {
    prefix: API_DATA_PLATFORM_PREFIX,
    pathParams: {
      viewId,
    },
    data: updateParams,
    mockCode: 'task-def-views.update',
  });
}

/**
 * Delete a task definition view
 * @param viewId
 */
export async function deleteTaskDefinitionView(viewId: string) {
  return delet<TaskDefinitionView>('/task-def-views/:viewId', {
    prefix: API_DATA_PLATFORM_PREFIX,
    pathParams: {
      viewId,
    },
    mockCode: 'task-def-views.delete',
  });
}

/**
 * Put task definitions into view
 * @param viewId
 * @param taskDefinitionIds
 */
export async function putTaskDefinitionsIntoView(viewId: string, taskDefinitionIds: string[]) {
  return patch<TaskDefinition[]>('/task-def-views/:viewId/task-definitions', {
    prefix: API_DATA_PLATFORM_PREFIX,
    pathParams: {
      viewId,
    },
    data: {
      taskDefinitionIds,
    },
    mockCode: 'task-def-views.put-task-defs-into-view',
  });
}

/**
 * Remove task definitions from view
 * @param viewId
 * @param taskDefinitionIds
 */
export async function removeTaskDefinitionsFromView(viewId: string, taskDefinitionIds: string[]) {
  return delet<TaskDefinition[]>('/task-def-views/:viewId/task-definitions', {
    prefix: API_DATA_PLATFORM_PREFIX,
    pathParams: {
      viewId,
    },
    data: {
      taskDefinitionIds,
    },
    mockCode: 'task-def-views.remove-task-defs-from-view',
  });
}

/**
 * Overwrite task definitions of a view
 * @param viewId
 * @param taskDefinitionIds
 */
export async function overwriteIncludingTaskDefinitionsOfView(viewId: string, taskDefinitionIds: string[]) {
  return put<TaskDefinition[]>('/task-def-views/:viewId/task-definitions', {
    prefix: API_DATA_PLATFORM_PREFIX,
    pathParams: {
      viewId,
    },
    data: {
      taskDefinitionIds,
    },
    mockCode: 'task-def-views.overwrite-task-defs-of-view',
  });
}

export async function fetchAllTaskDefinitionsByViewId(viewId: string): Promise<TaskDefinition[]> {
  return get('/task-def-views/:viewId/task-definitions', {
    prefix: API_DATA_PLATFORM_PREFIX,
    pathParams: {
      viewId,
    },
    mockCode: 'task-def-views.get-task-defs-of-view',
  });
}
