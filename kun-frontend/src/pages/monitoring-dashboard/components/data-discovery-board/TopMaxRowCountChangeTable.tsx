import React, { memo, useMemo } from 'react';
import { ColumnProps } from 'antd/es/table';
import { RowCountChange } from '@/services/monitoring-dashboard';
import { Card, Table } from 'antd';
import isNaN from 'lodash/isNaN';
import isFinite from 'lodash/isFinite';
import numeral from 'numeral';
import useI18n from '@/hooks/useI18n';

interface OwnProps {
  data: RowCountChange[];
}

type Props = OwnProps;

export const TopMaxRowCountChangeTable: React.FC<Props> = memo(function TopMaxRowCountChangeTable({ data }) {
  const t = useI18n();

  const columns: ColumnProps<RowCountChange>[] = useMemo(() => [
    {
      key: 'ordinal',
      title: '#',
      render: (txt: any, record: RowCountChange, index: number) => <span>{index + 1}</span>
    },
    {
      dataIndex: 'datasetName',
      key: 'datasetName',
      title: t('monitoringDashboard.dataDiscovery.maxRowCountChangeTable.datasetName'),
    },
    {
      dataIndex: 'database',
      key: 'database',
      title: t('monitoringDashboard.dataDiscovery.maxRowCountChangeTable.database'),
    },
    {
      dataIndex: 'dataSource',
      key: 'dataSource',
      title: t('monitoringDashboard.dataDiscovery.maxRowCountChangeTable.datasource'),
    },
    {
      dataIndex: 'rowChange',
      key: 'rowChange',
      align: 'right',
      title: t('monitoringDashboard.dataDiscovery.maxRowCountChangeTable.rowChange'),
      render: (txt: string | number) => <span>{numeral(txt).format('0,0')}</span>,
    },
    {
      dataIndex: 'rowCount',
      key: 'rowCount',
      align: 'right',
      title: t('monitoringDashboard.dataDiscovery.maxRowCountChangeTable.rowCount'),
      render: (txt: string | number) => <span>{numeral(txt).format('0,0')}</span>,
    },
    {
      dataIndex: 'rowChangeRatio',
      key: 'rowChangeRatio',
      align: 'right',
      title: t('monitoringDashboard.dataDiscovery.maxRowCountChangeTable.changeRatio'),
      render: (txt: any, record: RowCountChange) => {
        const ratio = Number(`${record.rowChangeRatio}`) * 100;
        if (isNaN(ratio) || !isFinite(ratio)) {
          return <span>N/A</span>;
        }
        if (ratio < 0) {
          return <span className="color-negative">
            {`${ratio}%`}
          </span>;
        }
        if (ratio > 0) {
          return <span className="color-positive">
            {`+${numeral(ratio).format('0,0.00')}%`}
          </span>;
        }
        // else
        return <span>{ratio}</span>;
      },
    },
  ], [t]);

  return (
    <Card bodyStyle={{ padding: '8px 8px 16px 8px' }}>
      <h3>{t('monitoringDashboard.dataDiscovery.maxRowCountChangeTable.title')}</h3>
      <Table<RowCountChange>
        dataSource={data}
        size="small"
        rowKey={r => `${r.datasetName}-${r.dataSource}-${r.rowChange}-${r.rowChangeRatio}-${r.rowCount}`}
        columns={columns}
        pagination={false}
      />
    </Card>
  );
});
