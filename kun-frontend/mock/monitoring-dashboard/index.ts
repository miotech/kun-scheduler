import { DEFAULT_API_PREFIX } from '../../mock-commons/constants/prefix';
import { getMockMetadataMetrics } from './get-metadata-metrics';
import { getMaxRowCountChange } from './get-max-row-count-change';
import { getFailedTestCases } from './get-failed-test-cases';

export default {
  [`GET ${DEFAULT_API_PREFIX}/dashboard/metadata/metrics`]: getMockMetadataMetrics,
  [`GET ${DEFAULT_API_PREFIX}/dashboard/metadata/max-row-count-change`]: getMaxRowCountChange,
  [`GET ${DEFAULT_API_PREFIX}/dashboard/test-cases`]: getFailedTestCases,
};
