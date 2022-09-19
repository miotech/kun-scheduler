import { get } from '@/utils/requestUtils';
import { API_DATA_PLATFORM_PREFIX } from '@/constants/api-prefixes';

import { TaskRunDiagnosis } from '@/definitions/TaskRun.type';
import { ServiceRespPromise } from '@/definitions/common-types';

// TODO: add mock code
/**
 * Fetch latest data diagnosis detail list by limit
 * @GET /task-diagnosis/:taskDefId/latest
 * @param taskDefId
 * @param query
 * @returns
 */
export const fetchLatestTaskDiagnosis = (
  taskDefId: string,
  query: { limit: number } = { limit: 7 },
): ServiceRespPromise<TaskRunDiagnosis[]> => {
  return get('/task-diagnosis/:taskDefId/latest', {
    query,
    pathParams: { taskDefId },
    mockCode: 'latest-task-diagnosis.search',
    prefix: API_DATA_PLATFORM_PREFIX,
  });
};

/**
 * Fetch latest data diagnosis detail
 * @GET /task-diagnosis/:taskDefId
 * @param taskDefId
 * @returns
 */
export const fetchTaskDiagnosisDetail = (taskDefId: string): ServiceRespPromise<TaskRunDiagnosis> => {
  return get('/task-diagnosis/:taskDefId', {
    pathParams: { taskDefId },
    mockCode: 'task-diagnosis.search',
    prefix: API_DATA_PLATFORM_PREFIX,
  });
};
