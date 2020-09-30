import { Request, Response } from 'express';
import Mock from 'mockjs';
import { wrapResponseData } from '../../mock-commons/utils/wrap-response';

function generateList(pageSize: number) {
  const mockData = Mock.mock({
    [`list|${pageSize}`]: [{
      datasetName: '@word(8, 20)',
      database: '@word(5, 15)',
      dataSource: '@word(5, 20)',
      rowChange: '@integer(0, 50000)',
      rowCount: '@integer(0, 100000)',
    }],
  });
  return mockData.list.map((d: any) => ({
    ...d,
    rowChangeRatio: d.rowChange / d.rowCount,
  }));
}

/**
 * mockCode: monitoring-dashboard.get-max-row-count-change
 */
export function getMaxRowCountChange(req: Request, res: Response) {
  const pageSize = Number(`${req.query.pageSize}`) || 10;
  return res.json(wrapResponseData({
    rowCountChanges: generateList(pageSize),
  }));
}
