import React, { memo, useCallback, useMemo, useState } from 'react';
import dayjs from 'dayjs';
import useI18n from '@/hooks/useI18n';
import useRedux from '@/hooks/useRedux';

import { Badge, Button, Popconfirm, Space, Table } from 'antd';
import { PauseOutlined, PlayCircleOutlined } from '@ant-design/icons';
import { UsernameText } from '@/components/UsernameText';

import { BackfillDetail } from '@/definitions/Backfill.type';
import { ColumnsType } from 'antd/es/table';
import { RunStatusEnum } from '@/definitions/StatEnums.type';
import { BackfillInstanceLogViewer } from '@/pages/operation-center/backfill-tasks/components/BackfillInstanceLogViewer';
import { BackfillTaskRunsSubTable } from './BackfillTaskRunsSubTable';

interface OwnProps {
  pageNum: number;
  pageSize: number;
  total: number;
  loading: boolean;
  data: BackfillDetail[];
  onClickStopBackfill: (backfill: BackfillDetail) => any;
  onClickRerunBackfill: (backfill: BackfillDetail) => any;
  onClickStopTaskRun: (taskRunId: string) => any;
  onClickRerunTaskRun: (taskRunId: string) => any;
}

type Props = OwnProps;

function renderByTaskRunListStatus(status: RunStatusEnum[]) {
  if (status.some(s => s === 'ABORTED' || s === 'ABORTING')) {
    return <Badge status="error" text="ABORTED" />;
  }
  if (status.some(s => s === 'RUNNING')) {
    return <Badge status="processing" text="RUNNING" />;
  }
  if (status.some(s => s === 'FAILED')) {
    return <Badge status="error" text="FAILED" />;
  }
  if (status.every(s => s === 'SUCCESS' || s === 'SKIPPED')) {
    return <Badge status="success" text="SUCCESS" />;
  }
  if (status.some(s => s === 'CREATED' || s === 'QUEUED')) {
    return <Badge status="warning" text="PENDING" />;
  }
  // else
  return <Badge status="default" text="UNKNOWN" />;
}

function backfillIsAlreadyComplete(status: RunStatusEnum[]): boolean {
  return status.every(
    s =>
      s === 'SUCCESS' ||
      s === 'SKIPPED' ||
      s === 'FAILED' ||
      s === 'ABORTED' ||
      s === 'ABORTING',
  );
}

export const BackfillTable: React.FC<Props> = memo(function BackfillTable(
  props,
) {
  const {
    pageNum,
    pageSize,
    total,
    loading,
    data,
    onClickStopBackfill,
    onClickRerunBackfill,
    onClickStopTaskRun,
    onClickRerunTaskRun,
  } = props;

  const { dispatch } = useRedux(() => {});

  const [viewingLogTaskTryId, setViewingLogTaskTryId] = useState<string | null>(
    null,
  );

  const t = useI18n();

  const handleChangePagination = useCallback(
    (nextPageNum: number, nextPageSize?: number) => {
      dispatch.backfillTasks.setTablePageNum(nextPageNum);
      dispatch.backfillTasks.setTablePageSize(nextPageSize ?? 25);
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [],
  );

  const handleClickViewLog = useCallback((taskTryId: string | null) => {
    if (!taskTryId) {
      return;
    }
    // else
    setViewingLogTaskTryId(taskTryId);
  }, []);

  const columns = useMemo<ColumnsType<BackfillDetail>>(
    () => [
      {
        title: t('operationCenter.backfill.property.id'),
        dataIndex: 'id',
        key: 'id',
        width: 180,
      },
      {
        title: t('operationCenter.backfill.property.name'),
        dataIndex: 'name',
        key: 'name',
      },
      {
        title: t('operationCenter.backfill.property.status'),
        key: 'status',
        render: (txt: any, record: BackfillDetail) => {
          return renderByTaskRunListStatus(
            record.taskRunList.map(r => r.status),
          );
        },
      },
      {
        title: t('operationCenter.backfill.property.tasksCount'),
        width: 120,
        key: 'flowTasksCount',
        render: (txt, record) => (
          <span>{(record.taskRunIds || []).length}</span>
        ),
      },
      {
        title: t('operationCenter.backfill.property.creator'),
        dataIndex: 'creator',
        width: 240,
        key: 'creator',
        render: (txt, record) => (
          <span>
            <UsernameText userId={record.creator} />
          </span>
        ),
      },
      {
        title: t('operationCenter.backfill.property.createTime'),
        dataIndex: 'createTime',
        key: 'createTime',
        width: 240,
        render: (txt, record) => {
          return (
            <span>
              {dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss')}
            </span>
          );
        },
      },
      {
        title: '',
        key: 'operations',
        width: 280,
        render: (txt, record) => {
          const stopDisabled = backfillIsAlreadyComplete(
            record.taskRunList.map(taskRun => taskRun.status),
          );
          return (
            <span>
              <Space size="small">
                <Popconfirm
                  title={t('operationCenter.backfill.operation.stopAll.alert')}
                  disabled={stopDisabled}
                  onConfirm={() => {
                    onClickStopBackfill(record);
                  }}
                >
                  <Button
                    icon={<PauseOutlined />}
                    size="small"
                    danger
                    disabled={stopDisabled}
                  >
                    {/* 停止所有任务 */}
                    {t('operationCenter.backfill.operation.stopAll')}
                  </Button>
                </Popconfirm>
                <Popconfirm
                  title={t('operationCenter.backfill.operation.rerun.alert')}
                  onConfirm={() => {
                    onClickRerunBackfill(record);
                  }}
                >
                  <Button icon={<PlayCircleOutlined />} size="small">
                    {/* 重新运行 */}
                    {t('operationCenter.backfill.operation.rerun')}
                  </Button>
                </Popconfirm>
              </Space>
            </span>
          );
        },
      },
    ],
    [onClickRerunBackfill, onClickStopBackfill, t],
  );

  return (
    <React.Fragment>
      <Table
        loading={loading}
        dataSource={data || []}
        columns={columns}
        size="small"
        bordered
        rowKey="id"
        expandedRowRender={record => (
          <BackfillTaskRunsSubTable
            data={record.taskRunList || []}
            taskDefinitionIds={record.taskDefinitionIds}
            onClickViewLog={handleClickViewLog}
            onClickStopTaskRun={onClickStopTaskRun}
            onClickRerunTaskRun={onClickRerunTaskRun}
          />
        )}
        pagination={{
          showQuickJumper: true,
          showSizeChanger: true,
          pageSizeOptions: ['10', '25', '50', '100'],
          current: pageNum,
          pageSize,
          total,
          onChange: handleChangePagination,
          showTotal: (_total: number) =>
            t('common.pagination.showTotal', { total: _total }),
        }}
      />
      <BackfillInstanceLogViewer
        visible={viewingLogTaskTryId != null}
        taskRunId={viewingLogTaskTryId}
        onClose={() => {
          setViewingLogTaskTryId(null);
        }}
      />
    </React.Fragment>
  );
});
