import { Request, Response } from 'express';
import { loremIpsum } from 'lorem-ipsum';
import { wrapResponseData } from '../../mock-commons/utils/wrap-response';

let logs: string[] = [];

export function fetchTaskTryLogs(req: Request, res: Response) {
  logs.push(loremIpsum());

  return res.json(wrapResponseData({
    attempt: 1,
    endLine: req.query?.endLine || logs.length,
    startLine: req.query?.startLine || 1,
    taskRunId: req.params?.id,
    logs,
  }));
}

export default {};
