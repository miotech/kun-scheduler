import { Request, Response } from 'express';
import Mock, { Random } from 'mockjs';
import { wrapResponseData } from '../../mock-commons/utils/wrap-response';
// @ts-ignore

function generateData() {
  const rawData = Mock.mock({
    dataQualityCoveredCount: Random.integer(0,1000),
    dataQualityLongExistingFailedCount: Random.integer(0,1000),
    dataQualityPassCount: Random.integer(0,1000),
    totalDatasetCount: Random.integer(0,1000),
  });
  rawData.totalCaseCount = rawData.dataQualityPassCount +
    rawData.dataQualityCoveredCount +
    rawData.dataQualityLongExistingFailedCount;
  return rawData;
}

/**
 * mockCode: monitoring-dashboard.get-metadata-metrics
 */
export function getMockMetadataMetrics(req: Request, res: Response) {
  return res.json(wrapResponseData(generateData()));
}
