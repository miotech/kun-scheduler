import {
  DimensionConfigItem,
  DataQualityReq,
  ValidateStatus,
  DataQualityResp,
  RelatedTableItem,
  DataQualityHistory,
} from '@/rematch/models/dataQuality';
import { delet, get, post } from '@/utils/requestUtils';
import { DEFAULT_API_PREFIX } from '@/constants/api-prefixes';
import { DataQualityItem } from '@/rematch/models/datasetDetail';
import { Pagination } from '@/definitions/common-types';

export interface FetchDimensionConfigRespBody {
  dimensionConfigs: DimensionConfigItem[];
}

export interface FetchDataAllQualitiesRespBody extends Pagination {
  dqCases: DataQualityItem[];
}

export async function fetchDataAllQualitiesService(datasetId: string, pagination: Pagination) {
  const { pageSize, pageNumber } = pagination;
  const params = {
    gid: datasetId,
    pageSize,
    pageNumber,
  };
  return get<FetchDataAllQualitiesRespBody>('/data-qualities', {
    prefix: DEFAULT_API_PREFIX,
    query: params,
  });
}

export async function fetchDataQualityService(id: string) {
  return get<DataQualityResp>('/data-quality/:id', {
    pathParams: {
      id,
    },
    prefix: DEFAULT_API_PREFIX,
  });
}

export async function fetchDimensionConfig(datasourceType: string) {
  return get<FetchDimensionConfigRespBody>('/data-quality/dimension/get-config', {
    query: { datasourceType },
    prefix: DEFAULT_API_PREFIX,
  });
}

export interface FetchValidateSQLServiceRespBody {
  validateStatus: ValidateStatus;
  relatedTables: RelatedTableItem[];
  validateMessage: string;
}

export async function fetchValidateSQLService(sqlText: string, datasetId: string, allRuleList: object[]) {
  const resp = await post<FetchValidateSQLServiceRespBody>('/sql/validate', {
    data: {
      sqlText,
      datasetId,
      validateRules: allRuleList,
    },
    prefix: DEFAULT_API_PREFIX,
  });
  return resp;
}

export async function addDataQualityService(params: DataQualityReq) {
  return post<{ id: string }>('/data-quality/add', {
    data: params,
    prefix: DEFAULT_API_PREFIX,
  });
}

export interface EditQualityReq extends DataQualityReq {}

export async function editQualityService(id: string, params: EditQualityReq) {
  return post<{ id: string }>('/data-quality/:id/edit', {
    pathParams: { id },
    data: params,
    prefix: DEFAULT_API_PREFIX,
  });
}

export async function deleteQualityService(id: string) {
  return delet<{ id: string }>('/data-quality/:id/delete', {
    pathParams: { id },
    prefix: DEFAULT_API_PREFIX,
  });
}

export interface DataQualityHistoryRespItem {
  caseId: string;
  historyList: DataQualityHistory[];
}

export type FetchDataQualityHistoriesResp = DataQualityHistoryRespItem[];

export async function fetchDataQualityHistoriesService(caseIds: string[]) {
  return get<FetchDataQualityHistoriesResp>('/data-quality/history', {
    query: { caseIds },
    prefix: DEFAULT_API_PREFIX,
  });
}
