import { ServiceRespPromise } from '@/definitions/common-types';
import { TaskTemplate } from '@/definitions/TaskTemplate.type';
import { get } from '@/utils/requestUtils';
import { API_DATA_PLATFORM_PREFIX } from '@/constants/api-prefixes';

/**
 * Fetch Task Templates
 * GET /api/task-templates
 */

export interface GetTaskTemplatesReqParams {
  name?: string;
}

export async function fetchTaskTemplates(reqParams: GetTaskTemplatesReqParams = {}): ServiceRespPromise<TaskTemplate[]> {
  return get('/task-templates', {
    query: {
      name: reqParams.name,
    },
    prefix: API_DATA_PLATFORM_PREFIX,
    mockCode: 'task-templates.get',
  });
}
