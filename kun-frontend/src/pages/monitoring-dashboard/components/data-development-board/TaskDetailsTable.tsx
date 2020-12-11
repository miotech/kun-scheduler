import React, { memo, useMemo } from 'react';
import { Table, Card, message, Checkbox, Space } from 'antd';
import { history } from 'umi';
import dayjs from 'dayjs';
import isNil from 'lodash/isNil';
import useI18n from '@/hooks/useI18n';
import SafeUrlAssembler from 'safe-url-assembler';

import { ColumnProps } from 'antd/es/table';
import { DevTaskDetail } from '@/services/monitoring-dashboard';
import { TableOnChangeCallback } from '@/definitions/common-types';
import getUniqId from '@/utils/getUniqId';
import { getTaskDefinitionIdByWorkflowTaskId } from '@/services/task-deployments/deployed-tasks';

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
        render: ((txt, record) => {
          return (
            <a
              href="#"
              onClick={async () => {
                const dismiss = message.loading('Loading...', 0);
                const taskDefinitionId = await getTaskDefinitionIdByWorkflowTaskId(record.taskId);
                if (taskDefinitionId) {
                  history.push(
                    SafeUrlAssembler()
                      .template('/data-development/task-definition/:taskDefId')
                      .param({
                        taskDefId: taskDefinitionId,
                      })
                      .toString(),
                  );
                } else {
                  message.error(`Cannot find related task definition for: ${record.taskName}`);
                }
                dismiss();
              }}
            >
              {record.taskName}
            </a>
          );
        }),
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
        dataIndex: 'createTime',
        key: 'createTime',
        title: t(
          'monitoringDashboard.dataDevelopment.taskDetailsTable.createTime',
        ),
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
        {!!total && <span style={{ marginLeft: 4 }}>({total})</span>}
        <span style={{ float: 'right' }}>
          <Space>
            {/* Radio button: display started tasks only */}
            <Checkbox
              disabled={props.displayStartedOnlyDisabled || false}
              checked={(!props.displayStartedOnlyDisabled) ? (props.displayStartedOnly || false) : false}
              onChange={(e) => {
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
              onChange={(e) => {
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
