import { Request, Response } from 'express';
import Mock from 'mockjs';
import { wrapResponseDataWithPagination } from '../../mock-commons/utils/wrap-response';

function generateList(pageSize: number): any[] {
  const mockData = Mock.mock({
    [`list|${pageSize}`]: [
      {
        result: '@pick(["SUCCESS", "FAILED", "UNKNOWN"])',
        errorReason: '@sentence()',
        updateTime: Date.now(),
        continuousFailingCount: '@integer(0, 10)',
        caseOwner: '@word()',
      },
    ],
  });
  return mockData.list;
}

/**
 * mockCode: monitoring-dashboard.get-failed-test-cases
 */
export function getFailedTestCases(req: Request, res: Response) {
  const pageNumber = Number(`${req.query.pageNumber}`) || 1;
  const pageSize = Number(`${req.query.pageSize}`) || 10;
  let list = generateList(pageSize);
  if (pageNumber > 5) {
    list = [];
  }
  return res.json(
    wrapResponseDataWithPagination(
      list,
      {
        pageNumber,
        pageSize,
        totalCount: 5 * pageSize,
      },
      'dataQualityCases',
    ),
  );
}
