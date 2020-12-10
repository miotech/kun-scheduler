import { get } from '@/utils/requestUtils';
import { DEFAULT_API_PREFIX } from '@/constants/api-prefixes';

export interface FetchMetadataMetricsResp {
  dataQualityCoveredCount: number;
  dataQualityLongExistingFailedCount: number;
  dataQualityPassCount: number;
  totalCaseCount: number;
  totalDatasetCount: number;
}

export async function fetchMetadataMetricsService() {
  return get<FetchMetadataMetricsResp>('/dashboard/metadata/metrics', {
    prefix: DEFAULT_API_PREFIX,
  });
}

export async function fetchMaxRowCountChange(pageSize: number) {
  return get<FetchMetadataMetricsResp>(
    '/dashbord/metadata/max-row-count-change',
    {
      query: {
        pageSize,
      },
      prefix: DEFAULT_API_PREFIX,
    },
  );
}
