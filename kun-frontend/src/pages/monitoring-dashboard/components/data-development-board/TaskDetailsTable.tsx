import React, { memo, useEffect, useMemo, useState } from 'react';
import { Table, Card, Checkbox, Space } from 'antd';
import dayjs from 'dayjs';
import isNil from 'lodash/isNil';
import useI18n from '@/hooks/useI18n';
import SafeUrlAssembler from 'safe-url-assembler';

import { ColumnProps } from 'antd/es/table';
import { DevTaskDetail } from '@/services/monitoring-dashboard';
import { TableOnChangeCallback } from '@/definitions/common-types';
import getUniqId from '@/utils/getUniqId';
import { getTaskDefinitionIdByWorkflowIds } from '@/services/task-deployments/deployed-tasks';
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

function getComputedLinkHref(taskDefIdsMap: Record<string, string | null>, taskId: string, taskRunId: string): string {
  if (!taskDefIdsMap[taskId]) {
    return '#';
  }
  // else
  return SafeUrlAssembler()
    .template('/operation-center/scheduled-tasks/:taskDefId')
    .param({
      taskDefId: taskDefIdsMap[taskId],
    })
    .query({
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

  const [definitionIdsLoading, setDefinitionIdsLoading] = useState<boolean>(false);
  const [workflowIdToTaskDefinitionIdMap, setWorkflowIdToTaskDefinitionIdMap] = useState<Record<string, string | null>>(
    {},
  );

  const t = useI18n();

  const workflowIds = useMemo(() => {
    return (data || []).map(datum => datum.taskId);
  }, [data]);

  useEffect(() => {
    setDefinitionIdsLoading(true);
    const effectAsync = async () => {
      try {
        if (workflowIds.length) {
          const workflowIdToTaskDefIdMapPayload = await getTaskDefinitionIdByWorkflowIds(workflowIds);
          setWorkflowIdToTaskDefinitionIdMap(workflowIdToTaskDefIdMapPayload);
        }
      } finally {
        setDefinitionIdsLoading(false);
      }
    };
    effectAsync();
  }, [workflowIds]);

  const columns: ColumnProps<DevTaskDetail>[] = useMemo(
    () => [
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
          const linkHref = getComputedLinkHref(workflowIdToTaskDefinitionIdMap, record.taskId, record.taskRunId);
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
        dataIndex: 'errorMessage',
        key: 'errorMessage',
        title: t('monitoringDashboard.dataDevelopment.taskDetailsTable.errorMessage'),
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
    ],
    [t, pageNum, pageSize, workflowIdToTaskDefinitionIdMap],
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
        loading={loading || definitionIdsLoading}
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
