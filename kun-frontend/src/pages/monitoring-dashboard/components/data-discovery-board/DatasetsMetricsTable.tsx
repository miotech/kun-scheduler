import React, { memo, useMemo } from 'react';
import { Card, Table } from 'antd';
import isNaN from 'lodash/isNaN';
import numeral from 'numeral';
import useI18n from '@/hooks/useI18n';
import getUniqId from '@/utils/getUniqId';
import { ColumnProps } from 'antd/es/table';
import { ColumnMetrics } from '@/services/monitoring-dashboard';
import { TableOnChangeCallback } from '@/definitions/common-types';

interface OwnProps {
  pageNum: number;
  pageSize: number;
  total: number;
  data: ColumnMetrics[];
  onChange?: TableOnChangeCallback<ColumnMetrics>;
}

type Props = OwnProps;

export const DatasetsMetricsTable: React.FC<Props> = memo(function DatasetsMetricsTable(props) {
  const {
    data,
    total,
    pageNum,
    pageSize,
    onChange,
  } = props;

  const t = useI18n();

  const columns: ColumnProps<ColumnMetrics>[] = useMemo(() => [
    {
      key: 'ordinal',
      title: '#',
      render: (txt: any, record: ColumnMetrics, index: number) =>
        <span>{((pageNum - 1) * pageSize) + index + 1}</span>,
    },
    {
      dataIndex: 'datasetName',
      key: 'datasetName',
      title: t('monitoringDashboard.dataDiscovery.datasetColumnMetricsTable.datasetName'),
    },
    {
      dataIndex: 'columnName',
      key: 'columnName',
      title: t('monitoringDashboard.dataDiscovery.datasetColumnMetricsTable.columnName'),
      ellipsis: true,
    },
    {
      dataIndex: 'columnNullRatio',
      key: 'columnNullRatio',
      align: 'right',
      title: t('monitoringDashboard.dataDiscovery.datasetColumnMetricsTable.columnNullRatio'),
      render: (txt, record) => {
        if (isNaN(Number(`${record.columnNullCount}`)) || !Number(`${record.totalRowCount}`)) {
          return <span>N/A</span>;
        }
        // else
        return <span>
          {`${numeral(record.columnNullCount / record.totalRowCount * 100).format('0,0.00')}%`}
        </span>;
      },
    },
    {
      dataIndex: 'columnNullCount',
      key: 'columnNullCount',
      align: 'right',
      title: t('monitoringDashboard.dataDiscovery.datasetColumnMetricsTable.columnNullRowCount'),
      render: (txt: string | number) => <span>{numeral(txt).format('0,0')}</span>
    },
    {
      dataIndex: 'totalRowCount',
      key: 'totalRowCount',
      align: 'right',
      title: t('monitoringDashboard.dataDiscovery.datasetColumnMetricsTable.totalRowCount'),
      render: (txt: string | number) => <span>{numeral(txt).format('0,0')}</span>
    },
  ], [
    t,
    pageNum,
    pageSize,
  ]);

  return (
    <Card bodyStyle={{ padding: '8px' }}>
      <h3>{t('monitoringDashboard.dataDiscovery.datasetColumnMetricsTable.title')}</h3>
      <Table<ColumnMetrics>
        dataSource={data}
        size="small"
        columns={columns}
        onChange={onChange}
        rowKey={r => `${getUniqId()}-${r.columnName}-${r.columnDistinctCount}-${r.columnNullCount}`}
        pagination={{
          current: pageNum,
          pageSize,
          total,
          simple: true,
        }}
      />
    </Card>
  );
});
