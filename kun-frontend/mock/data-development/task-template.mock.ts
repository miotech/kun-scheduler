import { Request, Response } from 'express';
import fs from 'fs';
import _ from 'lodash';
import path from 'path';
import yaml from 'js-yaml';
import FlakeId from 'flakeid';

import { MockDbUtil } from '../../mock-commons/utils/mock-db-util';
import { wrapResponseData, wrapResponseDataWithPagination, wrapResponseError } from '../../mock-commons/utils/wrap-response';

let templateMockData: any[] | null = null;
let taskDefinitionMockData: any[] | null = null;

const flakeId = new FlakeId({
  mid: 42,
  timeOffset: 1594003540433,
});

function init() {
  templateMockData = yaml.load(
    fs.readFileSync(path.resolve(__dirname, './task-template.mockdata.yaml'))
      .toString()
  );
  taskDefinitionMockData = yaml.load(
    fs.readFileSync(path.resolve(__dirname, './task-definition.mockdata.yaml'))
      .toString()
  );
}

/**
 * Mock API: Get Task Templates
 * Get list of task templates
 * URL: GET ${PREFIX}/task-templates
 * mockCode: 'task-templates.get'
 */

export function getTaskTemplates(req: Request, res: Response) {
  if (!templateMockData) {
    init();
  }
  return res.json(wrapResponseData(templateMockData));
}

/**
 * Mock API: search task definitions
 * URL: GET ${PREFIX}/task-definitions
 * mockCode: 'task-definitions.search'
 */

export function getTaskDefinitions(req: Request, res: Response) {
  if (!templateMockData) {
    init();
  }
  return res.json(wrapResponseDataWithPagination(taskDefinitionMockData || [], {
    pageNum: 1,
    pageSize: 100,
  }));
}

/**
 * Mock API: search task definitions detail
 * URL: GET ${PREFIX}/task-definitions/:id
 * mockCode: 'task-definitions.get-detail',
 */

export function getTaskDefinitionDetail(req: Request, res: Response) {
  if (!templateMockData) {
    init();
  }
  const taskDef = _.find(taskDefinitionMockData, taskDef => `${taskDef.id}` === `${req.params?.id}`);
  if (!taskDef) {
    return res.status(404).json(
      wrapResponseError(
        new Error(`Cannot find task definition with id ${req.params.id}`),
        404
      )
    );
  }
  return res.json(wrapResponseData(taskDef));
}


/**
 * Mock API: create task definition
 * URL: POST ${PREFIX}/task-definitions
 * mockCode: 'task-definitions.create',
 */

export function createTaskDefinition(req: Request, res: Response) {
  if (req.method !== 'POST') {
    return res.status(405).json({
      code: 405,
      note: 'Method not allowed',
    });
  }
  const { taskTemplateName, name } = req.body as {
    taskTemplateName: string;
    name: string;
  };
  const taskDefDB = MockDbUtil.getDatabaseInstance('taskDefinition');
  if (!taskDefDB.success) {
    return res
      .status(500)
      .json(wrapResponseError(new Error('Cannot get database'), 500));
  }
  // if success
  const taskDef = {
    id: flakeId.gen(),
    definitionId: 1,
    name,
    taskTemplateName,
    taskPayload: null,
    creator: 1,
    archived: false,
  };
  taskDefDB.set(taskDef.id, taskDef);
  return res.json(wrapResponseData(taskDef));
}

// export empty object
export default {};

