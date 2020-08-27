import { API_DATA_PLATFORM_PREFIX } from '../../mock-commons/constants/prefix';
import {
  getTaskTemplates,
  createTaskDefinition,
  getTaskDefinitions,
  getTaskDefinitionDetail,
} from './task-template.mock';
import { fetchTaskTryLogs } from './task-tries';
import {
  mockFetchDeployedTaskDetail,
  mockFetchTaskRuns,
  mockSearchDeployedTasks,
} from './deployed-tasks';
import { mockSearchDatasetAndRelatedTaskDefinition } from './task-datasets';

export default {
  [`GET ${API_DATA_PLATFORM_PREFIX}/task-templates`]: getTaskTemplates,
  [`GET ${API_DATA_PLATFORM_PREFIX}/task-definitions`]: getTaskDefinitions,
  [`GET ${API_DATA_PLATFORM_PREFIX}/task-definitions/:id`]: getTaskDefinitionDetail,
  [`POST ${API_DATA_PLATFORM_PREFIX}/task-definitions`]: createTaskDefinition,
  [`GET ${API_DATA_PLATFORM_PREFIX}/task-tries/:id/log`]: fetchTaskTryLogs,
  [`GET ${API_DATA_PLATFORM_PREFIX}/deployed-tasks`]: mockSearchDeployedTasks,
  [`GET ${API_DATA_PLATFORM_PREFIX}/deployed-tasks/:id`]: mockFetchDeployedTaskDetail,
  [`GET ${API_DATA_PLATFORM_PREFIX}/deployed-taskruns`]: mockFetchTaskRuns,
  [`GET ${API_DATA_PLATFORM_PREFIX}/deployed-tasks/:id/taskruns`]: mockFetchTaskRuns,
  [`GET ${API_DATA_PLATFORM_PREFIX}/data-sets/_search`]: mockSearchDatasetAndRelatedTaskDefinition,
};
