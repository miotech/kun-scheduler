import { DatasetDetail, Column } from '@/rematch/models/datasetDetail';
import { Pagination } from '@/rematch/models';
import { get, post } from './utils';

export interface FetchDatasetDetailRespBody extends DatasetDetail {}

export async function fetchDatasetDetailService(detailId: string) {
  const resp = await get<FetchDatasetDetailRespBody>(
    `/metadata/dataset/${detailId}`,
  );
  return resp;
}

export interface FetchDatasetColumnsRespBody extends Pagination {
  columns: Column[];
}

export async function fetchDatasetColumnsService(
  datasetId: string,
  keyword: string,
  pagination: Pagination,
) {
  const { pageSize, pageNumber } = pagination;
  const params = {
    keyword,
    pageSize,
    pageNumber,
  };
  const resp = await get<FetchDatasetColumnsRespBody>(
    `/metadata/dataset/${datasetId}/columns`,
    params,
  );
  return resp;
}

export interface PullDatasetRespBody {
  duration: number;
}

export async function pullDatasetService(datasetId: string) {
  const resp = await post<PullDatasetRespBody>(
    `/metadata/dataset/${datasetId}/pull`,
  );
  return resp;
}

export interface UpdateDatasetReqBody {
  description: string | null;
  owners: string[] | null;
  tags: string[] | null;
}

export interface UpdateDatasetRespBody extends FetchDatasetDetailRespBody {}

export async function updateDatasetService(
  datasetId: string,
  reqBody: UpdateDatasetReqBody,
) {
  const resp = await post<UpdateDatasetRespBody>(
    `/metadata/dataset/${datasetId}/update`,
    reqBody,
  );
  return resp;
}

export interface UpdateColumnRespBody {
  id: string;
  name: string;
  description: string;
}

export async function updateColumnService(
  id: string,
  params: { description: string },
) {
  const resp = await post<UpdateColumnRespBody>(
    `/metadata/column/${id}/update`,
    params,
  );
  return resp;
}
