import { DatasetTaskDefSummary, TaskDatasetProperty } from '@/definitions/DatasetTaskDefSummary.type';
import { getFlattenedTaskDefinition, getTaskDefinitionsFromFlattenedProps } from '../transformDataset';

const testInput1: DatasetTaskDefSummary[] = [
  {
    datastoreId: '10001',
    name: 'dataset-1',
    taskDefinitions: [
      {
        id: '20001',
        name: 'taskdef-1',
      },
      {
        id: '20002',
        name: 'taskdef-2',
      },
      {
        id: '20003',
        name: 'taskdef-3',
      },
    ],
  },
  {
    datastoreId: '10002',
    name: 'dataset-2',
    taskDefinitions: [],
  },
  {
    datastoreId: '10003',
    name: 'dataset-3',
    taskDefinitions: [
      {
        id: '20004',
        name: 'taskdef-4',
      },
    ],
  },
];

const expection1: TaskDatasetProperty[] = [
  {
    definitionId: '20001',
    datastoreId: '10001',
    datasetName: 'dataset-1',
  },
  {
    definitionId: '20002',
    datastoreId: '10001',
    datasetName: 'dataset-1',
  },
  {
    definitionId: '20003',
    datastoreId: '10001',
    datasetName: 'dataset-1',
  },
  {
    definitionId: '20004',
    datastoreId: '10003',
    datasetName: 'dataset-3',
  },
];

const upstreamTaskDefs = [
  {
    id: '20001',
    name: 'taskdef-1',
  },
  {
    id: '20002',
    name: 'taskdef-2',
  },
  {
    id: '20003',
    name: 'taskdef-3',
  },
  {
    id: '20004',
    name: 'taskdef-4',
  },
];

test('getFlattenedTaskDefinition should work properly', () => {
  const result1 = getFlattenedTaskDefinition(testInput1);
  expect(result1).toEqual(expection1);

  const result2 = getFlattenedTaskDefinition([]);
  expect(result2).toEqual([]);

  expect(() => {
    getFlattenedTaskDefinition([{ id: 1, name: '2', taskDefinitions: 3 }] as any)
  }).toThrow(Error);
});

test('getTaskDefinitionsFromFlattenedProps should work properly', () => {
  const flattened = getFlattenedTaskDefinition(testInput1);

  const result = getTaskDefinitionsFromFlattenedProps(flattened, upstreamTaskDefs);

  expect(result).toEqual(testInput1.filter(item => item.taskDefinitions.length > 0));
});
