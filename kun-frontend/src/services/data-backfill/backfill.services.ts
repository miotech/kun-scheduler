import {
  AcknowledgementVO,
  PaginationReqBody,
  PaginationRespBody,
  ServiceRespPromise,
} from '@/definitions/common-types';
import { Backfill, BackfillDetail } from '@/definitions/Backfill.type';
import { delet, get, post, put } from '@/utils/requestUtils';
import { API_DATA_PLATFORM_PREFIX } from '@/constants/api-prefixes';
import { TaskRun } from '@/definitions/TaskRun.type';

export interface SearchBackfillParams extends PaginationReqBody {
  name?: string;
  creatorIds?: string[];
  timeRngStart?: string;
  timeRngEnd?: string;
  sortKey?: string;
  sortOrder?: 'ASC' | 'DESC';
}

export async function fetchBackfillPage(
  searchParams: SearchBackfillParams,
): ServiceRespPromise<PaginationRespBody<Backfill>> {
  return get('/backfills', {
    query: {
      pageNum: searchParams.pageNumber || 1,
      pageSize: searchParams.pageSize || 100,
      name: searchParams.name,
      creatorIds: searchParams.creatorIds,
      timeRngStart: searchParams.timeRngStart,
      timeRngEnd: searchParams.timeRngEnd,
      sortKey: searchParams.sortKey,
      sortOrder: searchParams.sortOrder,
    },
    prefix: API_DATA_PLATFORM_PREFIX,
  });
}

export interface CreateAndRunBackfillReqParams {
  name: string;
  workflowTaskIds: string[];
  taskDefinitionIds: string[];
}

export async function createAndRunBackfill(
  params: CreateAndRunBackfillReqParams,
): ServiceRespPromise<any> {
  return post('/backfills', {
    data: {
      ...params,
    },
    prefix: API_DATA_PLATFORM_PREFIX,
  });
}

export async function fetchBackfillDetail(
  backfillId: string,
): ServiceRespPromise<BackfillDetail> {
  return get('/backfills/:backfillId', {
    pathParams: {
      backfillId,
    },
    prefix: API_DATA_PLATFORM_PREFIX,
  });
}

export async function stopBackfillInstance(
  backfillId: string,
): ServiceRespPromise<AcknowledgementVO> {
  return delet('/backfills/:backfillId/_abort', {
    pathParams: { backfillId },
    prefix: API_DATA_PLATFORM_PREFIX,
  });
}

export async function rerunBackfillInstance(
  backfillId: string,
): ServiceRespPromise<AcknowledgementVO> {
  return post('/backfills/:backfillId/_run', {
    pathParams: { backfillId },
    prefix: API_DATA_PLATFORM_PREFIX,
  });
}

export async function restartBackfillTaskRunInstance(
  taskRunId: string,
): ServiceRespPromise<TaskRun> {
  return post('/backfills/taskruns/:taskRunId/_restart', {
    pathParams: {
      taskRunId,
    },
    prefix: API_DATA_PLATFORM_PREFIX,
  });
}

export async function abortBackfillTaskRunInstance(
  taskRunId: string,
): ServiceRespPromise<TaskRun> {
  return put('/backfills/taskruns/:taskRunId/_abort', {
    pathParams: {
      taskRunId,
    },
    prefix: API_DATA_PLATFORM_PREFIX,
  });
}
