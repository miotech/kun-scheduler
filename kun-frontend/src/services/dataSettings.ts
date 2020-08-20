import {
  DataSource,
  DatasourceInfo,
  UpdateDatasourceInfo,
  DatabaseTypeList,
} from '@/rematch/models/dataSettings';
import { Pagination, Sort } from '@/rematch/models';

import { get, post, deleteFunc } from './utils';

export interface SearchDataBasesRespBody extends Pagination, Sort {
  datasources: DataSource[];
}

export async function fetchDatabaseTypesService() {
  const resp = await get<DatabaseTypeList>('/metadata/datasource/types');

  return resp;
}

export async function searchDataBasesService(
  search: string,
  pagination: Pagination,
) {
  const { pageSize, pageNumber } = pagination;

  const params = {
    pageSize,
    pageNumber,
    search,
  };

  const resp = await get<SearchDataBasesRespBody>(
    '/metadata/datasources',
    params,
  );

  return resp;
}

export async function addDatabaseService(reqBody: DatasourceInfo) {
  const resp = await post<DataSource>('/metadata/datasource/add', reqBody);
  return resp;
}

export async function updateDatabaseService(reqBody: UpdateDatasourceInfo) {
  const { id, ...others } = reqBody;
  const resp = await post<DataSource>(
    `/metadata/datasource/${id}/update`,
    others,
  );
  return resp;
}

export interface PullDatasetsFromDatabaseResp {
  table_count: number;
  duration: number;
}

export async function pullDatasetsFromDatabaseService(id: string) {
  const resp = await post<PullDatasetsFromDatabaseResp>(
    `/metadata/datasource/${id}/pull`,
  );
  return resp;
}

export interface DeleteDatabaseResp {
  id: string;
}

export async function deleteDatabaseService(id: string) {
  const resp = await deleteFunc<DeleteDatabaseResp>(
    `/metadata/datasource/${id}`,
  );
  return resp;
}
