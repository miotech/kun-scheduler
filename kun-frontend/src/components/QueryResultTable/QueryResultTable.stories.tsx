import React, { useRef } from 'react';
import { Meta } from '@storybook/react/types-6-0';
import { QueryResultTable } from './QueryResultTable.component';
import { SQLQueryRow } from '../../definitions/QueryResult.type';
import { createDndContext, DndProvider } from 'react-dnd';
import { HTML5Backend } from 'react-dnd-html5-backend';
import { DragDropManager } from 'dnd-core';

export default {
  title: 'Components/QueryResultTable',
  component: QueryResultTable,
} as Meta;

const COLUMN_NAMES_DEMO = [
  'id',
  'company_id',
  'namecn',
  'emailcn',
  'addresscn',
  'phonecn',
  'webtype',
  'website',
];

const DEMO_RECORDS: SQLQueryRow[] = [
  [
    '26c8516543a3d13264b730ece15073cd',
    '123',
    'foo@foo.com',
    'foo@foo.cn',
    '+86 1231231234',
    'webtype-1',
    'webname-1',
    'www.1.website.com',
  ],
  [
    "26c8516543a3d13264b730ece15073ce",
    '124',
    'bar@foo.com',
    'bar@foo.cn',
    '+86 1231235678',
    'webtype-2',
    'This is a really long long long long long long long long field name',
    'www.2.website.com',
  ],
  [
    "26c8516543a3d13264b730ece15073cf",
    '125',
    null,
    'abc@foo.cn',
    '+86 1231231224',
    null,
    null,
    'www.3.website.com',
  ],
];

export const QueryResultTableDemo = () => {
  const manager = useRef(createDndContext(HTML5Backend));

  return (
    <DndProvider
      manager={manager.current.dragDropManager as DragDropManager}
    >
      <QueryResultTable
        columnNames={COLUMN_NAMES_DEMO}
        pageNum={1}
        pageSize={5}
        total={10}
        data={DEMO_RECORDS}
      />
    </DndProvider>
  );
};
