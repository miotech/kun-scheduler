import Mock from 'mockjs';
import { DatasetTaskDefSummary } from '@/definitions/DatasetTaskDefSummary.type';

const schema = {
  'datastoreId': '@flakeId()',
  'name': '@word(10,20)',
  'taskDefinitions|0-5': [{
    'id': '@flakeId()',
    'name': '@word(15,25)',
  }]
};

export const DatasetTaskDefSummarySchema = {
  schema,
  listSchema: {
    'list|0-25': [
      schema,
    ],
  },
  generate(): DatasetTaskDefSummary {
    return Mock.mock(this.schema);
  },
  generateList(): DatasetTaskDefSummary[] {
    const mockData = Mock.mock(this.listSchema);
    return mockData.list;
  },
};
