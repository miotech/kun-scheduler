import React, { memo, useMemo } from 'react';
import { Table } from 'antd';
import { ColumnProps } from 'antd/lib/table';
import { FailedTestCaseRule } from '@/services/monitoring-dashboard';
import useI18n from '@/hooks/useI18n';

interface Props {
  data: FailedTestCaseRule[];
}

export default memo(function TestCaseRuleTable({ data }: Props) {
  const t = useI18n();

  const columns: ColumnProps<FailedTestCaseRule>[] = useMemo(
    () => [
      {
        title: t('monitoringDashboard.testCaseRuleTable.originalValue'),
        dataIndex: 'originalValue',
        width: 100,
      },
      {
        title: t('monitoringDashboard.testCaseRuleTable.field'),
        dataIndex: 'field',
        width: 100,
      },
      {
        title: t('monitoringDashboard.testCaseRuleTable.operator'),
        dataIndex: 'operator',
        width: 70,
      },
      {
        title: t('monitoringDashboard.testCaseRuleTable.expectedType'),
        dataIndex: 'expectedType',
        width: 100,
      },
      {
        title: t('monitoringDashboard.testCaseRuleTable.expectedValue'),
        dataIndex: 'expectedValue',
        width: 100,
      },
    ],
    [t],
  );

  return (
    <Table
      dataSource={data}
      columns={columns}
      pagination={false}
      size="small"
    />
  );
});
