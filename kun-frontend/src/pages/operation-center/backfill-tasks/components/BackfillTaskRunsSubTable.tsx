import React, { memo, useMemo } from 'react';
import { TaskRun } from '@/definitions/TaskRun.type';
import { Badge, Button, Popconfirm, Table, Tooltip } from 'antd';
import { ColumnsType } from 'antd/es/table';
import useI18n from '@/hooks/useI18n';
import { Link } from 'umi';
import { RunStatusEnum } from '@/definitions/StatEnums.type';
import dayjs from 'dayjs';

import Duration from 'dayjs/plugin/duration';
import {
  FileSearchOutlined,
  PauseOutlined,
  PlayCircleOutlined,
} from '@ant-design/icons';

dayjs.extend(Duration);

interface OwnProps {
  data: TaskRun[];
  taskDefinitionIds: string[];
  onClickViewLog: (taskTryId: string | null) => any;
  onClickStopTaskRun: (taskRunId: string) => any;
  onClickRerunTaskRun: (taskRunId: string) => any;
}

type Props = OwnProps;

function renderStatus(status: RunStatusEnum) {
  switch (status) {
    case 'RUNNING':
      return <Badge status="processing" text={status} />;
    case 'CREATED':
    case 'QUEUED':
      return <Badge status="warning" text={status} />;
    case 'ABORTED':
    case 'ABORTING':
    case 'FAILED':
      return <Badge status="error" text={status} />;
    case 'SUCCESS':
      return <Badge status="success" text={status} />;
    default:
      return <Badge status="default" text={status} />;
  }
}

function isStoppedStatus(status: RunStatusEnum): boolean {
  return (
    status === 'SKIPPED' ||
    status === 'SUCCESS' ||
    status === 'ABORTING' ||
    status === 'ABORTED' ||
    status === 'FAILED'
  );
}

export const BackfillTaskRunsSubTable: React.FC<Props> = memo(
  function BackfillTaskRunsSubTable(props) {
    const t = useI18n();

    const columns = useMemo<ColumnsType<TaskRun>>(
      () => [
        {
          title: t('operationCenter.backfill.taskrun.id'),
          key: 'id',
          dataIndex: 'id',
          width: 180,
          render: (txt, record) => (
            <span>
              <Button
                size="small"
                type="link"
                onClick={() => {
                  props.onClickViewLog(record.attempts?.[0].taskRunId ?? null);
                }}
              >
                {txt}
              </Button>
            </span>
          ),
        },
        {
          title: t('operationCenter.backfill.taskrun.taskName'),
          key: 'taskName',
          render: (txt: any, record: TaskRun, index: number) => {
            return (
              <Link
                to={`/data-development/task-definition/${props.taskDefinitionIds[index]}`}
              >
                {record.task.name}
              </Link>
            );
          },
        },
        {
          title: t('operationCenter.backfill.taskrun.status'),
          key: 'status',
          dataIndex: 'status',
          render: (txt, record) => {
            return renderStatus(record.status);
          },
        },
        {
          title: t('operationCenter.backfill.taskrun.startTime'),
          key: 'startAt',
          dataIndex: 'startAt',
          width: 240,
          render: (txt, record) =>
            record.startAt
              ? dayjs(record.startAt).format('YYYY-MM-DD HH:mm:ss')
              : '-',
        },
        {
          title: t('operationCenter.backfill.taskrun.endTime'),
          key: 'endAt',
          dataIndex: 'endAt',
          width: 240,
          render: (txt, record) =>
            record.endAt
              ? dayjs(record.endAt).format('YYYY-MM-DD HH:mm:ss')
              : '-',
        },
        {
          title: t('operationCenter.backfill.taskrun.duration'),
          key: 'duration',
          width: 240,
          render: (txt, record) => {
            if (
              record.startAt == null ||
              record.endAt == null ||
              dayjs(record.endAt)
                .toDate()
                .getTime() <
                dayjs(record.startAt)
                  .toDate()
                  .getTime()
            ) {
              return '-';
            }
            // else
            const duration = dayjs.duration(
              dayjs(record.endAt).diff(dayjs(record.startAt)),
            );
            return `${duration.hours()}:${duration.minutes()}:${duration.seconds()}`;
          },
        },
        {
          title: '',
          key: 'operations',
          width: 64,
          render: (txt, record) => {
            return (
              <Button.Group size="small">
                <Tooltip
                  title={t('operationCenter.backfill.taskrun.operation.stop')}
                >
                  <Popconfirm
                    title={t('operationCenter.taskrun.operation.stop.alert')}
                    disabled={isStoppedStatus(record.status)}
                    onConfirm={() => {
                      props.onClickStopTaskRun(record.id);
                    }}
                  >
                    <Button
                      icon={<PauseOutlined />}
                      disabled={isStoppedStatus(record.status)}
                    />
                  </Popconfirm>
                </Tooltip>
                <Tooltip
                  title={t('operationCenter.backfill.taskrun.operation.rerun')}
                >
                  <Popconfirm
                    title={t('operationCenter.taskrun.operation.rerun.alert')}
                    onConfirm={() => {
                      props.onClickRerunTaskRun(record.id);
                    }}
                  >
                    <Button icon={<PlayCircleOutlined />} />
                  </Popconfirm>
                </Tooltip>
                <Tooltip
                  title={t('operationCenter.backfill.taskrun.operation.logs')}
                >
                  <Button
                    onClick={() => {
                      props.onClickViewLog(
                        record.attempts?.[0].taskRunId ?? null,
                      );
                    }}
                    icon={<FileSearchOutlined />}
                  />
                </Tooltip>
              </Button.Group>
            );
          },
        },
      ],
      // eslint-disable-next-line react-hooks/exhaustive-deps
      [
        props.taskDefinitionIds,
        props.onClickRerunTaskRun,
        props.onClickStopTaskRun,
        t,
      ],
    );

    return (
      <Table
        columns={columns}
        dataSource={props.data}
        size="small"
        pagination={false}
        bordered
        rowKey="id"
      />
    );
  },
);
