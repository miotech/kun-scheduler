import React, { memo, useCallback, useEffect, useMemo, useState } from 'react';
import { useRouteMatch, Link } from 'umi';
import { Badge, Button, Card, Descriptions, Popconfirm, Skeleton, Space } from 'antd';
import dayjs from 'dayjs';
import { useUnmount } from 'ahooks';

import useRedux from '@/hooks/useRedux';
import useI18n from '@/hooks/useI18n';
import { BackfillTaskRunsSubTable } from '@/pages/operation-center/backfill-tasks/components/BackfillTaskRunsSubTable';
import { BackfillInstanceLogViewer } from '@/pages/operation-center/backfill-tasks/components/BackfillInstanceLogViewer';
import {
  abortBackfillTaskRunInstance,
  rerunBackfillInstance,
  restartBackfillTaskRunInstance,
  stopBackfillInstance,
} from '@/services/data-backfill/backfill.services';
import { UsernameText } from '@/components/UsernameText';
import { RunStatusEnum } from '@/definitions/StatEnums.type';
import Icon, { ArrowLeftOutlined, ReloadOutlined } from '@ant-design/icons';
import { ReactComponent as RerunIcon } from '@/assets/icons/rerun.svg';
import { ReactComponent as StopIcon } from '@/assets/icons/stop.svg';

import { BackfillDetail } from '@/definitions/Backfill.type';
import css from './BackfillDetailView.module.less';

interface OwnProps {}

type Props = OwnProps;

function backfillIsAlreadyComplete(status: RunStatusEnum[]): boolean {
  return status.every(s => s === 'SUCCESS' || s === 'SKIPPED' || s === 'FAILED' || s === 'ABORTED' || s === 'ABORTING');
}

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

export const BackfillDetailView: React.FC<Props> = memo(function BackfillDetailView() {
  const match = useRouteMatch<{ id: string }>(); // id is the backfill id
  const backfillId = match.params.id;
  const t = useI18n();

  const {
    selector: { isLoading, backfillDetailData, pageError, tableIsReloading },
    dispatch,
  } = useRedux(s => ({
    isLoading: s.backfillTasks.backfillDetail.isLoading,
    backfillDetailData: s.backfillTasks.backfillDetail.data,
    pageError: s.backfillTasks.backfillDetail.pageError,
    tableIsReloading: s.backfillTasks.backfillDetail.tableIsReloading,
  }));

  const [logViewerTaskRunId, setLogViewerTaskRunId] = useState<string | null>(null);

  const reloadTable = useCallback(async () => {
    try {
      await dispatch.backfillTasks.refreshBackfillSubTasksTable(backfillId);
    } catch (e) {
      // do nothing
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [backfillId]);

  useEffect(() => {
    if (backfillId) {
      dispatch.backfillTasks.loadBackfillDetail(backfillId);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [backfillId]);

  useUnmount(() => {
    dispatch.backfillTasks.setBackfillDetailData(null);
  });

  const handleCloseLogViewer = useCallback(function handleCloseLogViewer() {
    setLogViewerTaskRunId(null);
  }, []);

  const handleStopBackfillInstance = useCallback(
    async function handleStopBackfillInstance(backfill: BackfillDetail) {
      try {
        await stopBackfillInstance(backfill.id);
      } finally {
        reloadTable();
      }
    },
    [reloadTable],
  );

  const handleRerunBackfillInstance = useCallback(
    async function handleRerunBackfillInstance(backfill: BackfillDetail) {
      try {
        await rerunBackfillInstance(backfill.id);
      } finally {
        reloadTable();
      }
    },
    [reloadTable],
  );

  const handleRestartTaskRun = useCallback(
    async function handleRestartTaskRun(taskRunId: string) {
      try {
        await restartBackfillTaskRunInstance(taskRunId);
      } finally {
        reloadTable();
      }
    },
    [reloadTable],
  );

  const handleAbortTaskRun = useCallback(
    async function handleAbortTaskRun(taskRunId: string) {
      try {
        await abortBackfillTaskRunInstance(taskRunId);
      } finally {
        reloadTable();
      }
    },
    [reloadTable],
  );

  const content = useMemo(() => {
    if (isLoading) {
      return <Skeleton active />;
    }
    if (pageError) {
      return pageError.message;
    }
    if (!backfillDetailData) {
      return <></>;
    }
    // else

    const descriptionHeading = (
      <>
        <h1 className={css.Title}>
          <Link to="/operation-center/backfill-tasks">
            <Button type="link" icon={<ArrowLeftOutlined />} />
          </Link>
          <span className={css.TitleText}>{backfillDetailData.name}</span>
        </h1>
        <Descriptions size="middle" bordered column={2}>
          <Descriptions.Item label={t('operationCenter.backfill.property.creator')}>
            <UsernameText userId={backfillDetailData.creator} />
          </Descriptions.Item>
          <Descriptions.Item label={t('operationCenter.backfill.property.createTime')}>
            {dayjs(backfillDetailData.createTime).format('YYYY-MM-DD HH:mm:ss')}
          </Descriptions.Item>
          <Descriptions.Item label={t('operationCenter.backfill.property.status')}>
            {renderByTaskRunListStatus((backfillDetailData.taskRunList || []).map(taskRun => taskRun.status))}
          </Descriptions.Item>
          <Descriptions.Item label={t('operationCenter.backfill.property.tasksCount')}>
            {(backfillDetailData.taskRunList || []).length}
          </Descriptions.Item>
        </Descriptions>
      </>
    );

    const stopDisabled = backfillIsAlreadyComplete(backfillDetailData.taskRunList.map(taskRun => taskRun.status));
    const actionToAllButtonsBar = (
      <div className={css.AllTasksActionBar}>
        <Space>
          {stopDisabled ? (
            <Popconfirm
              title={t('operationCenter.backfill.operation.rerun.alert')}
              onConfirm={() => {
                handleRerunBackfillInstance(backfillDetailData);
              }}
            >
              <Button loading={tableIsReloading} icon={<Icon component={RerunIcon} />}>
                {/* 重新运行 */}
                {t('operationCenter.backfill.operation.rerun')}
              </Button>
            </Popconfirm>
          ) : (
            <Popconfirm
              title={t('operationCenter.backfill.operation.stopAll.alert')}
              disabled={stopDisabled}
              onConfirm={() => {
                handleStopBackfillInstance(backfillDetailData);
              }}
            >
              <Button icon={<Icon component={StopIcon} />} danger loading={tableIsReloading} disabled={stopDisabled}>
                {/* 停止所有任务 */}
                {t('operationCenter.backfill.operation.stopAll')}
              </Button>
            </Popconfirm>
          )}
          <Button onClick={reloadTable} loading={tableIsReloading} icon={<ReloadOutlined />}>
            {t('common.refresh')}
          </Button>
        </Space>
      </div>
    );

    return (
      <React.Fragment>
        {descriptionHeading}
        {actionToAllButtonsBar}
        <div className={css.SubTasksTableWrapper}>
          <BackfillTaskRunsSubTable
            loading={tableIsReloading}
            data={backfillDetailData.taskRunList || []}
            onClickViewLog={taskTryId => {
              setLogViewerTaskRunId(taskTryId);
            }}
            onClickStopTaskRun={handleAbortTaskRun}
            onClickRerunTaskRun={handleRestartTaskRun}
          />
        </div>
      </React.Fragment>
    );
  }, [
    isLoading,
    pageError,
    backfillDetailData,
    t,
    reloadTable,
    tableIsReloading,
    handleAbortTaskRun,
    handleRestartTaskRun,
  ]);

  return (
    <div id="backfill-detail-view" className={css.View}>
      <Card className={css.ContentCard}>{content}</Card>
      <BackfillInstanceLogViewer
        visible={logViewerTaskRunId != null}
        taskRunId={logViewerTaskRunId}
        onClose={handleCloseLogViewer}
      />
    </div>
  );
});
