import { Request, Response } from 'express';
import { wrapResponseData } from '../../mock-commons/utils/wrap-response';
import { DatasetTaskDefSummarySchema } from './schemas/DatasetTaskDefSummary.schema';

/**
 * mockCode: 'datasets.searchRelatedTaskDefs'
 */
export function mockSearchDatasetAndRelatedTaskDefinition(req: Request, res: Response) {
  const payload = wrapResponseData(
    DatasetTaskDefSummarySchema.generateList()
  );
  return res.status(200).json(payload);
}

export default {};
