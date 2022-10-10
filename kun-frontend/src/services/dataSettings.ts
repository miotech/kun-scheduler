import { Pagination, Sort } from '@/definitions/common-types';
import { DataSourceForm, DataSourceInfo } from '@/definitions/DataSource.type';

import { delet, get, post } from '@/utils/requestUtils';
import { DEFAULT_API_PREFIX } from '@/constants/api-prefixes';
import { RunStatusEnum } from '@/definitions/StatEnums.type';
import { DatasetPullProcessVO } from './datasetDetail';

export interface SearchDataBasesRespBody extends Pagination, Sort {
  datasources: DataSourceInfo[];
}

export async function searchDataBasesService(search: string, pagination: Pagination) {
  const { pageSize, pageNumber } = pagination;

  const params = {
    pageSize,
    pageNumber,
    search,
  };

  return get<SearchDataBasesRespBody>('/metadata/datasources', {
    query: params,
    prefix: DEFAULT_API_PREFIX,
  });
}

export async function getUserConnection(id: string) {
  return get('/metadata/datasource/user-connection/:id', {
    pathParams: { id },
    prefix: DEFAULT_API_PREFIX,
  });
}

export async function addDatabaseService(reqBody: DataSourceForm) {
  return post('/metadata/datasource/add', {
    data: reqBody,
    prefix: DEFAULT_API_PREFIX,
  });
}

export async function updateDatabaseService(reqBody: DataSourceInfo) {
  const { id, ...others } = reqBody;
  return post('/metadata/datasource/:id/update', {
    pathParams: { id },
    data: others,
    prefix: DEFAULT_API_PREFIX,
  });
}

export interface PullDatasetsFromDatabaseResp {
  table_count: number;
  duration: number;
}

export async function pullDatasetsFromDatabaseService(id: string) {
  return post<DatasetPullProcessVO>('/metadata/datasource/:id/pull', {
    pathParams: { id },
    prefix: DEFAULT_API_PREFIX,
  });
}

export type DataSourcePullProcessVO = {
  processId: string;
  processType: 'DATASOURCE';
  createdAt: string;
  latestMCETaskRun: PullTaskRunInfo | null;
  latestMSETaskRun: PullTaskRunInfo | null;
};

export type PullTaskRunInfo = {
  id: string;
  status: RunStatusEnum;
  startAt: string;
  endAt: string;
  createdAt: string;
  updatedAt: string;
};

export async function fetchLatestPullProcessesOfDataSources(dataSourceIds: string[]) {
  if (!dataSourceIds?.length) {
    return {};
  }
  return get<Record<string, DataSourcePullProcessVO>>('/metadata/datasource/processes/latest', {
    query: { dataSourceIds: dataSourceIds.join(',') },
    prefix: DEFAULT_API_PREFIX,
  });
}

export interface DeleteDatabaseResp {
  id: string;
}

export async function deleteDatabaseService(id: string) {
  return delet<DeleteDatabaseResp>('/metadata/datasource/:id', {
    pathParams: { id },
    prefix: DEFAULT_API_PREFIX,
  });
}

export async function getDatabaseService(id: string) {
  return get('/metadata/datasource/:id', {
    pathParams: { id },
    prefix: DEFAULT_API_PREFIX,
  });
}
