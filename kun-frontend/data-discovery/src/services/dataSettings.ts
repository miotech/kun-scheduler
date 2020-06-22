import {
  DataBase,
  DatabaseInfo,
  UpdateDatabaseInfo,
  DatabaseTypeList,
} from '@/rematch/models/dataSettings';
import { Pagination, Sort } from '@/rematch/models';

import { get, post, deleteFunc } from './utils';

export interface SearchDataBasesRespBody extends Pagination, Sort {
  databases: DataBase[];
}

export async function fetchDatabaseTypesService() {
  const resp = await get<DatabaseTypeList>('/metadata/database/types');

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
    '/metadata/databases',
    params,
  );

  return resp;
}

export async function addDatabaseService(reqBody: DatabaseInfo) {
  const resp = await post<DataBase>('/metadata/database/add', reqBody);
  return resp;
}

export async function updateDatabaseService(reqBody: UpdateDatabaseInfo) {
  const { id, ...others } = reqBody;
  const resp = await post<DataBase>(`/metadata/database/${id}/update`, others);
  return resp;
}

export interface PullDatasetsFromDatabaseResp {
  table_count: number;
  duration: number;
}

export async function pullDatasetsFromDatabaseService(id: string) {
  const resp = await post<PullDatasetsFromDatabaseResp>(
    `/metadata/database/${id}/pull`,
  );
  return resp;
}

export interface DeleteDatabaseResp {
  id: string;
}

export async function deleteDatabaseService(id: string) {
  const resp = await deleteFunc<DeleteDatabaseResp>(`/metadata/database/${id}`);
  return resp;
}
