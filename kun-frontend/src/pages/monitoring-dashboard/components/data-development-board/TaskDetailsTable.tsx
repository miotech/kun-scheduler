import React, { memo, useMemo } from 'react';
import { Table, Card } from 'antd';
import dayjs from 'dayjs';
import isNil from 'lodash/isNil';
import useI18n from '@/hooks/useI18n';

import { ColumnProps } from 'antd/es/table';
import { DevTaskDetail } from '@/services/monitoring-dashboard';
import { TableOnChangeCallback } from '@/definitions/common-types';
import getUniqId from '@/utils/getUniqId';

interface OwnProps {
  pageNum: number;
  pageSize: number;
  total: number;
  data: DevTaskDetail[];
  onChange: TableOnChangeCallback<DevTaskDetail>;
  loading?: boolean;
}

type Props = OwnProps;

export const TaskDetailsTable: React.FC<Props> = memo(function TaskDetailsTable(
  props,
) {
  const { data, pageNum, pageSize, total, onChange, loading } = props;

  const t = useI18n();

  const columns: ColumnProps<DevTaskDetail>[] = useMemo(
    () => [
      {
        key: 'ordinal',
        title: '#',
        width: 60,
        render: (txt: any, record: DevTaskDetail, index: number) => (
          <span>{(pageNum - 1) * pageSize + index + 1}</span>
        ),
      },
      {
        dataIndex: 'taskName',
        key: 'taskName',
        title: t(
          'monitoringDashboard.dataDevelopment.taskDetailsTable.taskName',
        ),
      },
      {
        dataIndex: 'taskStatus',
        key: 'taskStatus',
        title: t(
          'monitoringDashboard.dataDevelopment.taskDetailsTable.taskStatus',
        ),
      },
      {
        dataIndex: 'errorMessage',
        key: 'errorMessage',
        title: t(
          'monitoringDashboard.dataDevelopment.taskDetailsTable.errorMessage',
        ),
      },
      {
        dataIndex: 'startTime',
        key: 'startTime',
        title: t(
          'monitoringDashboard.dataDevelopment.taskDetailsTable.startTime',
        ),
        render: (txt: any, record: DevTaskDetail) => (
          <span>
            {!isNil(record.startTime) && dayjs(record.startTime).isValid()
              ? dayjs(record.startTime).format('YYYY-MM-DD HH:mm')
              : '-'}
          </span>
        ),
      },
      {
        dataIndex: 'endTime',
        key: 'endTime',
        title: t(
          'monitoringDashboard.dataDevelopment.taskDetailsTable.endTime',
        ),
        render: (txt: any, record: DevTaskDetail) => (
          <span>
            {!isNil(record.endTime) && dayjs(record.endTime).isValid()
              ? dayjs(record.endTime).format('YYYY-MM-DD HH:mm')
              : '-'}
          </span>
        ),
      },
    ],
    [t, pageNum, pageSize],
  );

  return (
    <Card>
      <h3>
        {t('monitoringDashboard.dataDevelopment.taskDetailsTable.title')}
        {total && <span style={{ marginLeft: 4 }}>({total})</span>}
      </h3>
      <Table<DevTaskDetail>
        loading={loading}
        dataSource={data}
        size="small"
        columns={columns}
        onChange={onChange}
        rowKey={r =>
          `${getUniqId()}-${r.taskName}-${r.taskStatus}-${r.duration}-${
            r.startTime
          }-${r.endTime}`
        }
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
