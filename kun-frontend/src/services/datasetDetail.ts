import { DatasetDetail, Column } from '@/rematch/models/datasetDetail';
import { Pagination } from '@/rematch/models';
import { get, post } from './utils';

export interface FetchDatasetDetailRespBody extends DatasetDetail {}

export async function fetchDatasetDetailService(detailId: string) {
  const resp = await get<FetchDatasetDetailRespBody>(
    `/metadata/dataset/${detailId}`,
  );
  return resp;
  // // TODO: 假数据
  // return {
  //   id: '1',
  //   name: 'table1',
  //   schema: 'public',
  //   type: 'Postgres',
  //   high_watermark: {
  //     user: 'admin',
  //     time: 14000000000,
  //   },
  //   low_watermark: {
  //     user: 'admin',
  //     time: 14000000000,
  //   },
  //   description: 'dummy description',
  //   lifecycle: Lifecycle.PROCESS,
  //   owners: ['admin'],
  //   tags: ['a', 'b', 'c'],
  //   database: 'DEV Postgres',
  //   row_count: 1484,
  //   flows: [
  //     {
  //       flow_id: '1',
  //       flow_name: 'xxx',
  //     },
  //   ],
  // } as FetchDatasetDetailRespBody;
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
  // // TODO: jiashuju
  // return {
  //   columns: [
  //     {
  //       id: '1',
  //       name: 'table1',
  //       high_watermark: {
  //         user: 'admin',
  //         time: 140000000000,
  //       },
  //       description: 'dummy description',
  //       not_null_count: 1484,
  //       not_null_percentage: 0.99,
  //     },
  //   ],
  // };
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
