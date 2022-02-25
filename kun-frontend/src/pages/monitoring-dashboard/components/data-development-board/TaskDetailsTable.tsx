import React, { memo, useMemo } from 'react';
import { Table, Card, Checkbox, Space, Tooltip } from 'antd';
import { dayjs } from '@/utils/datetime-utils';
import isNil from 'lodash/isNil';
import useI18n from '@/hooks/useI18n';
import SafeUrlAssembler from 'safe-url-assembler';

import { ColumnProps } from 'antd/es/table';
import { DevTaskDetail } from '@/services/monitoring-dashboard';
import { TableOnChangeCallback } from '@/definitions/common-types';
import getUniqId from '@/utils/getUniqId';
import { StatusText } from '@/components/StatusText';

interface OwnProps {
  pageNum: number;
  pageSize: number;
  total: number;
  data: DevTaskDetail[];
  onChange: TableOnChangeCallback<DevTaskDetail>;
  loading?: boolean;
  displayStartedOnly?: boolean;
  displayLast24HoursOnly?: boolean;
  displayStartedOnlyDisabled?: boolean;
  onChangeDisplayStartedOnly?: (nextCheckState: boolean) => any;
  onChangeDisplayLast24HoursOnly?: (nextCheckState: boolean) => any;
}

type Props = OwnProps;

function getComputedLinkHref(taskId: string, taskRunId: string): string {
  return SafeUrlAssembler()
    .template('/operation-center/task-run-id/:taskRunId')
    .param({
      taskRunId,
    })
    .toString();
}

function shouldDisplayEndTime(record: DevTaskDetail): boolean {
  if (record.endTime != null && dayjs(record.endTime).isValid()) {
    const endMoment = dayjs(record.endTime);
    if (record.startTime != null && dayjs(record.startTime).isValid()) {
      const startMoment = dayjs(record.endTime);
      return endMoment.toDate().getTime() - startMoment.toDate().getTime() >= 0;
    }
    // else
    return true;
  }
  return false;
}

export const TaskDetailsTable: React.FC<Props> = memo(function TaskDetailsTable(props) {
  const { data, pageNum, pageSize, total, onChange, loading } = props;
  const t = useI18n();

  const columns: ColumnProps<DevTaskDetail>[] = useMemo(() => {
    const arr = [
      {
        key: 'ordinal',
        title: '#',
        width: 60,
        render: (txt: any, record: DevTaskDetail, index: number) => <span>{(pageNum - 1) * pageSize + index + 1}</span>,
      },
      {
        dataIndex: 'taskName',
        key: 'taskName',
        width: 280,
        title: t('monitoringDashboard.dataDevelopment.taskDetailsTable.taskName'),
        render: (txt, record) => {
          const linkHref = getComputedLinkHref(record.taskId, record.taskRunId);
          if (linkHref === '#') {
            return <span>{record.taskName}</span>;
          }
          // else
          return (
            <a href={linkHref} rel="noopener nofollow">
              {record.taskName}
            </a>
          );
        },
      },
      {
        dataIndex: 'taskStatus',
        key: 'taskStatus',
        title: t('monitoringDashboard.dataDevelopment.taskDetailsTable.taskStatus'),
        render: txt => <StatusText status={txt} />,
      },
      {
        dataIndex: 'createTime',
        key: 'createTime',
        title: t('monitoringDashboard.dataDevelopment.taskDetailsTable.createTime'),
        render: (txt: any, record: DevTaskDetail) => (
          <span>
            {!isNil(record.createTime) && dayjs(record.createTime).isValid()
              ? dayjs(record.createTime).format('YYYY-MM-DD HH:mm')
              : '-'}
          </span>
        ),
      },
      {
        dataIndex: 'startTime',
        key: 'startTime',
        title: t('monitoringDashboard.dataDevelopment.taskDetailsTable.startTime'),
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
        title: t('monitoringDashboard.dataDevelopment.taskDetailsTable.endTime'),
        render: (txt: any, record: DevTaskDetail) => (
          <span>
            {shouldDisplayEndTime(record) ? dayjs(record!.endTime as number).format('YYYY-MM-DD HH:mm') : '-'}
          </span>
        ),
      },
    ];
    if (data.findIndex(item => item.taskStatus === 'UPSTREAM_FAILED') > -1) {
      const column = {
        dataIndex: 'failedTask',
        key: 'failedTask',
        title: t('monitoringDashboard.dataDevelopment.taskDetailsTable.failedTask'),
        render: (txt: any, record: DevTaskDetail) => (
          <div>
            {record?.rootFailedTasks?.map(item => {
              const linkHref = getComputedLinkHref(item.taskId, item.taskRunId);
              return (
                <Tooltip title={item.taskName}>
                  <a href={linkHref} rel="noopener nofollow">
                    {item.taskName}
                  </a>
                  <br />
                </Tooltip>
              );
            })}
          </div>
        ),
      };
      arr.splice(3, 0, column);
    }
    const upstreamArr = data.filter(item => item.taskStatus === 'UPSTREAM_FAILED');
    if (upstreamArr.length === data.length) {
      arr.splice(5, 2);
    }
    return arr;
  }, [t, pageNum, pageSize, data]);

  return (
    <Card>
      <h3>
        {t('monitoringDashboard.dataDevelopment.taskDetailsTable.title')}
        {!!total && <span style={{ marginLeft: 4 }}>({total})</span>}
        <span style={{ float: 'right' }}>
          <Space>
            {/* Radio button: display started tasks only */}
            <Checkbox
              disabled={props.displayStartedOnlyDisabled || false}
              checked={!props.displayStartedOnlyDisabled ? props.displayStartedOnly || false : false}
              onChange={e => {
                const { checked } = e.target;
                if (props.onChangeDisplayStartedOnly) {
                  props.onChangeDisplayStartedOnly(checked);
                }
              }}
            >
              {t('monitoringDashboard.dataDevelopment.taskDetailsTable.displayStartedOnly')}
            </Checkbox>
            {/* Radio button: display tasks in 24 hours only */}
            <Checkbox
              checked={props.displayLast24HoursOnly || false}
              onChange={e => {
                const { checked } = e.target;
                if (props.onChangeDisplayLast24HoursOnly) {
                  props.onChangeDisplayLast24HoursOnly(checked);
                }
              }}
            >
              {t('monitoringDashboard.dataDevelopment.taskDetailsTable.display24HoursOnly')}
            </Checkbox>
          </Space>
        </span>
      </h3>
      <Table<DevTaskDetail>
        loading={loading}
        dataSource={data}
        tableLayout="fixed"
        size="small"
        columns={columns}
        onChange={onChange}
        rowKey={r => `${getUniqId()}-${r.taskName}-${r.taskStatus}-${r.duration}-${r.startTime}-${r.endTime}`}
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
