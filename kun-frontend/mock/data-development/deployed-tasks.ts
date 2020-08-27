import { Request, Response } from 'express';
import fs from 'fs';
import _ from 'lodash';
import path from 'path';
import yaml from 'js-yaml';
import {
  wrapResponseData,
  wrapResponseDataWithPagination,
  wrapResponseError,
} from '../../mock-commons/utils/wrap-response';
import { DeployedTask } from '@/definitions/DeployedTask.type';
import { DeployedTaskRunSchema } from './schemas/DeployedTaskRuns.schema';

const mockDeployedTasks: DeployedTask[] = yaml.load(
  fs.readFileSync(path.resolve(__dirname, './deployed-tasks.mockdata.yaml')).toString()
);

/**
 * mockCode: 'deployed-tasks.search'
 */
export function mockSearchDeployedTasks(req: Request, res: Response) {
  return res.json(wrapResponseDataWithPagination(mockDeployedTasks));
}

/**
 * mockCode: 'deployed-tasks.get-detail'
 */
export function mockFetchDeployedTaskDetail(req: Request, res: Response) {
  const task = _.find(mockDeployedTasks, t => `${t.id}` === req.params?.id);
  if (!task) {
    return res.status(404)
      .json(wrapResponseError(new Error(`Cannot find task detail with id: ${req.params?.id}`), 404));
  }
  return res.json(wrapResponseData(task));
}

/**
 * mockCode: 'deployed-taskruns.search'
 */
export function mockFetchTaskRuns(req: Request, res: Response) {
  return res.json(wrapResponseDataWithPagination(DeployedTaskRunSchema.generateList(), {
    pageNum: parseFloat(`${req.query.pageNum}`) || 1,
    pageSize: parseFloat(`${req.query.pageSize}`) || 25,
    totalCount: 100,
  }));
}

export default {};
