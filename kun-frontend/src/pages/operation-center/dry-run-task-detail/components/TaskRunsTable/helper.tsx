import React, { useMemo } from 'react';
import { Button, Tooltip, Popconfirm } from 'antd';
import Icon, { FileSearchOutlined } from '@ant-design/icons';

import useI18n from '@/hooks/useI18n';
import { isStoppedStatus, isRunningStatus } from '@/utils/task';
import { ReactComponent as StopIcon } from '@/assets/icons/stop-rect.svg';
import { ReactComponent as RerunIcon } from '@/assets/icons/rerun.svg';
import { RunStatusEnum } from '@/definitions/StatEnums.type';
import { StatusText } from '@/components/StatusText';
import { TaskRunMemoryDiagnosis } from '@/components/TasKRunDiagnosis/TaskRunMemoryDiagnosis';
import dayjs from 'dayjs';
import { DateFormat } from '@/definitions/date.type';
import { pick } from 'lodash';
import { durationFormatter } from '@/utils/datetime-utils';
import { TableColumnProps } from 'antd/es';
import { TaskRun } from '@/definitions/TaskRun.type';

export interface TaskAction {
  (instance: string): void;
  (taskRunId: string): void;
}

function generateColumn<T extends string>(
  title: string,
  dataIndex: string | string[],
  render?: (value: T, record: TaskRun) => React.ReactElement | string,
  options?: Omit<TableColumnProps<any>, 'title' | 'dataIndex' | 'render'>,
): TableColumnProps<any> {
  return {
    title,
    dataIndex,
    render,
    ...options,
  };
}

const renderOperation = (
  t: ReturnType<typeof useI18n>,
  stopTask: TaskAction,
  rerunTask: TaskAction,
  viewLog: TaskAction,
): ((value: any, record: any) => React.ReactElement) => {
  return (_, record: any) => {
    const status = record?.status;
    return (
      <Button.Group size="small">
        <Tooltip title={t('operationCenter.backfill.taskrun.operation.stop')}>
          <Popconfirm
            title={t('operationCenter.taskrun.operation.stop.alert')}
            disabled={isStoppedStatus(status)}
            onConfirm={() => {
              stopTask(record.id);
            }}
          >
            <Button icon={<Icon component={StopIcon} />} disabled={isStoppedStatus(status)} />
          </Popconfirm>
        </Tooltip>
        <Tooltip title={t('operationCenter.backfill.taskrun.operation.rerun')}>
          <Popconfirm
            title={t('operationCenter.taskrun.operation.rerun.alert')}
            onConfirm={() => {
              rerunTask(record.id);
            }}
          >
            <Button icon={<Icon component={RerunIcon} />} />
          </Popconfirm>
        </Tooltip>
        <Tooltip title={t('operationCenter.backfill.taskrun.operation.logs')}>
          <Button
            onClick={() => {
              viewLog(record.attempts?.[0].taskRunId ?? null);
            }}
            icon={<FileSearchOutlined />}
          />
        </Tooltip>
      </Button.Group>
    );
  };
};

const renderTaskStatus = (status: RunStatusEnum) => {
  return <StatusText status={status ?? ''} />;
};

const renderTime = (time: string) => {
  return time ? dayjs(time).format(DateFormat.Y_M_D_H_m_s) : '-';
};

const renderStartTime = (time: string, record: TaskRun) => {
  if (isStoppedStatus(record.status) || isRunningStatus(record.status)) {
    return renderTime(time);
  }
  return '-';
};

const renderEndTime = (time: string, record: TaskRun) => {
  if (isStoppedStatus(record.status)) {
    return renderTime(time);
  }
  return '-';
};

const renderDuration = (_: string, record: any) => {
  if (isStoppedStatus(record.status)) {
    const { startAt, endAt } = pick<typeof record>(record || {}, ['startAt', 'endAt']);
    if (!startAt || !endAt) return '-';

    return durationFormatter(dayjs.duration(dayjs(endAt).diff(dayjs(startAt))));
  }
  return '-';
};

const renderTaskRunMemoryDiagnosis = (_: unknown, record: TaskRun) => {
  return <TaskRunMemoryDiagnosis shouldModal taskRunId={record.id} />;
};

export const useColumns = (
  stopTask: TaskAction,
  rerunTask: TaskAction,
  viewLog: TaskAction,
): TableColumnProps<any>[] => {
  const t = useI18n();
  return useMemo(() => {
    return [
      generateColumn(t('operationCenter.dryRun.instance.column.id'), 'id', (instanceId: string, record?: TaskRun) => {
        return (
          <Button type="link" onClick={() => viewLog(record?.attempts?.[0].taskRunId ?? '')}>
            {instanceId}
          </Button>
        );
      }),
      generateColumn<RunStatusEnum>(t('operationCenter.dryRun.instance.column.status'), 'status', renderTaskStatus),
      generateColumn(t('operationCenter.dryRun.instance.column.startTime'), 'startAt', renderStartTime),
      generateColumn(t('operationCenter.dryRun.instance.column.endTime'), 'endAt', renderEndTime),
      generateColumn(t('operationCenter.dryRun.instance.column.duration'), '', renderDuration),
      generateColumn(t('operationCenter.dryRun.instance.column.memeory'), '', renderTaskRunMemoryDiagnosis),
      generateColumn('', '', renderOperation(t, stopTask, rerunTask, viewLog), { align: 'center', width: '40px' }),
    ];
  }, [stopTask, rerunTask, viewLog, t]);
};
