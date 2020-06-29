import { get } from '@/utils/requestUtils';
import { API_DATA_PLATFORM_PREFIX } from '@/constants/api-prefixes';
import { ServiceRespPromise } from '@/definitions/common-types';
import { DatasetTaskDefSummary } from '@/definitions/DatasetTaskDefSummary.type';

export interface SearchDatasetAndRelatedTaskDefsReqParams {
  dataStoreIds?: string[];
  definitionIds?: string[];
  name?: string;
}

export async function searchDatasetAndRelatedTaskDefs(
  reqParams: SearchDatasetAndRelatedTaskDefsReqParams = {}
): ServiceRespPromise<DatasetTaskDefSummary[]> {
  return get('/data-sets/_search', {
    query: {
      ...reqParams,
    },
    prefix: API_DATA_PLATFORM_PREFIX,
    mockCode: 'datasets.searchRelatedTaskDefs',
  });
}
