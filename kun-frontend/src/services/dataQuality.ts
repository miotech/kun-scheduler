import {
  DimensionConfigItem,
  DataQualityReq,
  ValidateStatus,
  DataQualityResp,
} from '@/rematch/models/dataQuality';
import { delet, get, post } from '@/utils/requestUtils';
import { DEFAULT_API_PREFIX } from '@/constants/api-prefixes';

export interface FetchDimensionConfigRespBody {
  dimensionConfigs: DimensionConfigItem[];
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
  return get<FetchDimensionConfigRespBody>(
    '/data-quality/dimension/get-config',
    {
      query: { datasourceType },
      prefix: DEFAULT_API_PREFIX,
    },
  );
}

export interface FetchValidateSQLServiceRespBody {
  validateStatus: ValidateStatus;
}

export async function fetchValidateSQLService(
  sqlText: string,
  datasetId: string,
) {
  const resp = await post<FetchValidateSQLServiceRespBody>('/sql/validate', {
    data: {
      sqlText,
      datasetId,
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

export interface DeleteQualityReq {
  datasetId: string;
}

export async function deleteQualityService(
  id: string,
  params: DeleteQualityReq,
) {
  return delet<{ id: string }>('/data-quality/:id/delete', {
    pathParams: { id },
    data: params,
    prefix: DEFAULT_API_PREFIX,
  });
}
