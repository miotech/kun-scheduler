import { LineageEdge, LineageNode } from '@/definitions/Lineage.type';

export const initialNodes: LineageNode[] = [
  {
    id: '81704446437888000',
    data: {
      id: '81704446437888000',
      name: 'dataset-1',
      database: 'mysql',
      datasource: 'foo',
      description: 'dataset-1-description',
      owners: ['admin'],
      glossaries: [],
      tags: [],
      type: 'mysql',
      schema: '',
      high_watermark: {
        user: '',
        time: 0,
      },
      expandableUpstream: true,
    },
  },
  {
    id: '81704446437888001',
    data: {
      id: '81704446437888001',
      name: 'dataset-2',
      database: 'mysql',
      datasource: 'foo',
      description: 'dataset-2-description',
      owners: ['admin'],
      glossaries: [],
      tags: [],
      type: 'mysql',
      schema: '',
      high_watermark: {
        user: '',
        time: 0,
      },
      expandableUpstream: true,
      expandableDownstream: true,
    },
  },
  {
    id: '81704446437888002',
    data: {
      id: '81704446437888002',
      name: 'dataset-3',
      database: 'postgres',
      datasource: 'foo',
      description: 'dataset-3-description',
      owners: ['admin'],
      glossaries: [],
      tags: [],
      type: 'postgres',
      schema: 'public',
      high_watermark: {
        user: '',
        time: 0,
      },
    },
  },
  {
    id: '81704446437888003',
    data: {
      id: '81704446437888003',
      name: 'dataset-4',
      database: 'hive',
      datasource: 'bar',
      description: 'dataset-4-description',
      owners: ['admin'],
      glossaries: [],
      tags: [],
      type: 'hive',
      schema: 'dm',
      high_watermark: {
        user: '',
        time: 0,
      },
    },
  },
  {
    id: '81704446437888004',
    data: {
      id: '81704446437888004',
      name: 'dataset-5',
      database: 'hive',
      datasource: 'bar',
      description: 'dataset-5-description',
      owners: ['admin'],
      glossaries: [],
      tags: [],
      type: 'hive',
      schema: 'dm',
      high_watermark: {
        user: '',
        time: 0,
      },
    },
  },
  {
    id: '81704446437888005',
    data: {
      id: '81704446437888005',
      name: 'dataset-6',
      database: 'hive',
      datasource: 'bar',
      description: 'dataset-6-description',
      owners: ['admin'],
      glossaries: [],
      tags: [],
      type: 'hive',
      schema: 'dm',
      high_watermark: {
        user: '',
        time: 0,
      },
    },
  },
];

export const initialEdges: LineageEdge[] = [
  { src: '81704446437888000', dest: '81704446437888002' },
  { src: '81704446437888001', dest: '81704446437888002' },
  { src: '81704446437888002', dest: '81704446437888003' },
  { src: '81704446437888002', dest: '81704446437888004' },
  { src: '81704446437888004', dest: '81704446437888005' },
];
