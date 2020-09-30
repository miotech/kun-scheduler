import React, { memo, useMemo } from 'react';
import { ColumnProps } from 'antd/es/table';
import { FailedTestCase } from '@/services/monitoring-dashboard';
import { Card, Table, Tooltip } from 'antd';
import dayjs from 'dayjs';
import useI18n from '@/hooks/useI18n';
import { TableOnChangeCallback } from '@/definitions/common-types';

interface OwnProps {
  data: FailedTestCase[];
  pageNum: number;
  pageSize: number;
  total: number;
  onChange?: TableOnChangeCallback<FailedTestCase>;
}

type Props = OwnProps;

export const FailedTestCasesTable: React.FC<Props> = memo((props) => {
  const {
    data,
    pageNum,
    pageSize,
    total,
    onChange,
  } = props;

  const t = useI18n();

  const columns: ColumnProps<FailedTestCase>[] = useMemo(() => [
    {
      key: 'ordinal',
      title: '#',
      render: (txt: any, record: FailedTestCase, index: number) =>
        <span>{((pageNum - 1) * pageSize) + index + 1}</span>
    },
    {
      dataIndex: 'result',
      key: 'result',
      title: t('monitoringDashboard.dataDiscovery.failedTestCasesTable.result'),
    },
    {
      dataIndex: 'errorReason',
      key: 'errorReason',
      title: t('monitoringDashboard.dataDiscovery.failedTestCasesTable.errorReason'),
      ellipsis: true,
      render: (txt) => {
        // TODO: replace with another presentation approach
        return (
          <Tooltip title={txt} placement="right">
            <div>
              {txt}
            </div>
          </Tooltip>
        );
      },
    },
    {
      dataIndex: 'updateTime',
      key: 'updateTime',
      align: 'right',
      sorter: true,
      title: t('monitoringDashboard.dataDiscovery.failedTestCasesTable.lastUpdatedTime'),
      render: (txt: number) => dayjs(txt).format('YYYY-MM-DD HH:mm'),
    },
    {
      dataIndex: 'continuousFailingCount',
      key: 'continuousFailingCount',
      align: 'right',
      sorter: true,
      title: t('monitoringDashboard.dataDiscovery.failedTestCasesTable.continuousFailingCount'),
    },
    {
      dataIndex: 'caseOwner',
      key: 'caseOwner',
      align: 'right',
      title: t('monitoringDashboard.dataDiscovery.failedTestCasesTable.caseOwner'),
    },
  ], [
    t,
    pageNum,
    pageSize,
  ]);

  return (
    <Card bodyStyle={{ padding: '8px' }}>
      <h3>{t('monitoringDashboard.dataDiscovery.failedTestCasesTable.title')}</h3>
      <Table<FailedTestCase>
        dataSource={data}
        size="small"
        columns={columns}
        onChange={onChange}
        rowKey={r => `${r.result}-${r.caseOwner}-${r.errorReason}-${r.updateTime}`}
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
