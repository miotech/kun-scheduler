import React from 'react';
import { TableRowSelection } from 'antd/es/table/interface';
import { Table as AntdTable } from 'antd';
import { TableProps, ColumnsType, TablePaginationConfig } from 'antd/es/table';
import c from 'classnames';
import './Table.less';

export default function Table<RecordType extends object = any>(
  props: TableProps<RecordType>,
) {
  const newProps: TableProps<RecordType> = {
    ...props,
    className: c(props.className, 'kun-table-component'),
  };
  return <AntdTable<RecordType> {...newProps} />;
}

export { TableProps, ColumnsType, TablePaginationConfig, TableRowSelection };
