import { get } from '@/utils/requestUtils';
import { DEFAULT_API_PREFIX } from '@/constants/api-prefixes';
import { LineageTask } from '@/rematch/models/datasetDetail';
import { Graph } from '@/rematch/models/lineage';

export enum LineageDirection {
  UPSTREAM = 'UPSTREAM',
  DOWNSTREAM = 'DOWNSTREAM',
}
export interface FetchLineageTasksReq {
  datasetGid: string;
  direction: LineageDirection;
}

export interface FetchLineageTasksResp {
  tasks: LineageTask[];
}

export async function fetchLineageTasksService(params: FetchLineageTasksReq) {
  return get<FetchLineageTasksResp>('/lineage/tasks', {
    query: params,
    prefix: DEFAULT_API_PREFIX,
  });
}

export interface FetchLineageRelatedTasksReq {
  sourceDatasetGid: string;
  destDatasetGid: string;
}

export async function fetchLineageRelatedTasksService(
  params: FetchLineageRelatedTasksReq,
) {
  return get<FetchLineageTasksResp>('/lineage/tasks', {
    query: params,
    prefix: DEFAULT_API_PREFIX,
  });
}

export interface FetchLineageGraphInfoRequestParams {
  datasetGid: string;
  direction?: LineageDirection;
  depth?: number;
}

export interface FetchLineageGraphInfoResp extends Graph {}

export async function fetchLineageGraphInfoService(
  params: FetchLineageGraphInfoRequestParams,
) {
  return get<FetchLineageGraphInfoResp>('/lineage/graph', {
    query: params,
  });
}
