import { get } from '@/utils/requestUtils';
import { DEFAULT_API_PREFIX } from '@/constants/api-prefixes';
import { Pagination } from '@/definitions/common-types';
import { LineageTask } from '@/rematch/models/datasetDetail';

export enum LineageDirection {
  UPSTREAM = 'UPSTREAM',
  DOWNSTREAM = 'DOWNSTREAM',
}
export interface FetchLineageTasksReq extends Pagination {
  datasetGid: string;
  direction: LineageDirection;
}

export interface FetchLineageTasksResp extends Pagination {
  tasks: LineageTask[];
}

export async function fetchLineageTasksService(params: FetchLineageTasksReq) {
  return get<FetchLineageTasksResp>('/lineage/tasks', {
    query: params,
    prefix: DEFAULT_API_PREFIX,
  });
}
