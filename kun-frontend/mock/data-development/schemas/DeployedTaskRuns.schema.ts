import Mock from 'mockjs';
import { TaskAttempt } from '@/definitions/TaskAttempt.type';

const schema = {
  'id': '@flakeId()',
  'attempts|1-3': [{
    'id': '@flakeId()',
    'endAt': '@time("yyyy-MM-dd HH:mm:ss")',
    'startAt': '@time("yyyy-MM-dd HH:mm:ss")',
    'status': "@pick('ABORTED', 'ABORTING', 'CREATED', 'FAILED', 'QUEUED', 'RETRY', 'RUNNING', 'SKIPPED', 'SUCCESS')",
    'taskId': '@flakeId()',
    'taskName': '@word(10, 20)',
    'taskRunId': '@flakeId()',
  }],
  'dependencyTaskRunIds|0-3': ['@flakeId()'],
  'inlets': [{ type: '@pick(["ARANGO_COLLECTION","ELASTICSEARCH_INDEX","FILE","HIVE_TABLE","MONGO_COLLECTION","MYSQL_TABLE","POSTGRES_TABLE","SHEET","TOPIC"])' }],
  'outlets': [{ type: '@pick(["ARANGO_COLLECTION","ELASTICSEARCH_INDEX","FILE","HIVE_TABLE","MONGO_COLLECTION","MYSQL_TABLE","POSTGRES_TABLE","SHEET","TOPIC"])' }],
  'scheduledTick': { time: '@time("yyyy-MM-dd HH:mm:ss")'},
  'tags|0-3': [{
    key: '@word()',
    value: '@word()',
  }],
  // TODO: complete task schema
  'task': {},
  'variables': [],
};

export const DeployedTaskRunSchema = {
  schema,
  listSchema: {
    'list|25': [
      schema,
    ],
  },
  generate() {
    const mockData = Mock.mock(this.schema);
    mockData.attempts.forEach((item: TaskAttempt, idx: number) => {
      item.attempt = idx + 1;
    });
    return mockData;
  },
  generateList() {
    const mockData = Mock.mock(this.listSchema);
    mockData.list.forEach((payload: any) => {
      payload.attempts.forEach((item: TaskAttempt, idx: number) => {
        item.attempt = idx + 1;
      });
    });
    return mockData.list;
  },
}
