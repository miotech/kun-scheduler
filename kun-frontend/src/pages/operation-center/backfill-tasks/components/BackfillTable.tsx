import React, { memo, useCallback, useMemo, useState } from 'react';
import dayjs from 'dayjs';
import useI18n from '@/hooks/useI18n';
import useRedux from '@/hooks/useRedux';
import { Link } from 'umi';

import { Table } from 'antd';
import { UsernameText } from '@/components/UsernameText';

import { Backfill } from '@/definitions/Backfill.type';
import { ColumnsType } from 'antd/es/table';
// import { RunStatusEnum } from '@/definitions/StatEnums.type';
import { BackfillInstanceLogViewer } from '@/pages/operation-center/backfill-tasks/components/BackfillInstanceLogViewer';

interface OwnProps {
  pageNum: number;
  pageSize: number;
  total: number;
  loading: boolean;
  data: Backfill[];
  // onClickStopBackfill: (backfill: BackfillDetail) => any;
  // onClickRerunBackfill: (backfill: BackfillDetail) => any;
  // onClickStopTaskRun: (taskRunId: string) => any;
  // onClickRerunTaskRun: (taskRunId: string) => any;
}

type Props = OwnProps;

// function backfillIsAlreadyComplete(status: RunStatusEnum[]): boolean {
//   return status.every(s => s === 'SUCCESS' || s === 'SKIPPED' || s === 'FAILED' || s === 'ABORTED' || s === 'ABORTING');
// }

export const BackfillTable: React.FC<Props> = memo(function BackfillTable(props) {
  const { pageNum, pageSize, total, loading, data } = props;

  const { dispatch } = useRedux(() => {});

  const [viewingLogTaskTryId, setViewingLogTaskTryId] = useState<string | null>(null);

  const t = useI18n();

  const handleChangePagination = useCallback(
    (nextPageNum: number, nextPageSize?: number) => {
      dispatch.backfillTasks.setTablePageNum(nextPageNum);
      dispatch.backfillTasks.setTablePageSize(nextPageSize ?? 25);
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [],
  );

  // const handleClickViewLog = useCallback((taskTryId: string | null) => {
  //   if (!taskTryId) {
  //     return;
  //   }
  //   // else
  //   setViewingLogTaskTryId(taskTryId);
  // }, []);

  const columns = useMemo<ColumnsType<Backfill>>(
    () => [
      {
        title: t('operationCenter.backfill.property.id'),
        dataIndex: 'id',
        key: 'id',
        width: 180,
        render: (txt, record) => <Link to={`/operation-center/backfill-tasks/${record.id}`}>{record.id}</Link>,
      },
      {
        title: t('operationCenter.backfill.property.name'),
        dataIndex: 'name',
        key: 'name',
      },
      {
        title: t('operationCenter.backfill.property.tasksCount'),
        width: 120,
        key: 'flowTasksCount',
        render: (txt, record) => <span>{(record.taskRunIds || []).length}</span>,
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
          return <span>{dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss')}</span>;
        },
      },
      // {
      //   title: '',
      //   key: 'operations',
      //   width: 280,
      //   render: (txt, record) => {
      //     const stopDisabled = backfillIsAlreadyComplete(record.taskRunList.map(taskRun => taskRun.status));
      //     return (
      //       <span>
      //         <Space size="small">
      //           {stopDisabled ? (
      //             <Popconfirm
      //               title={t('operationCenter.backfill.operation.rerun.alert')}
      //               onConfirm={() => {
      //                 onClickRerunBackfill(record);
      //               }}
      //             >
      //               <Button icon={<Icon component={RerunIcon} />} size="small">
      //                 {/* 重新运行 */}
      //                 {t('operationCenter.backfill.operation.rerun')}
      //               </Button>
      //             </Popconfirm>
      //           ) : (
      //             <Popconfirm
      //               title={t('operationCenter.backfill.operation.stopAll.alert')}
      //               disabled={stopDisabled}
      //               onConfirm={() => {
      //                 onClickStopBackfill(record);
      //               }}
      //             >
      //               <Button icon={<Icon component={StopIcon} />} size="small" danger disabled={stopDisabled}>
      //                 {/* 停止所有任务 */}
      //                 {t('operationCenter.backfill.operation.stopAll')}
      //               </Button>
      //             </Popconfirm>
      //           )}
      //         </Space>
      //       </span>
      //     );
      //   },
      // },
    ],
    [t],
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
        pagination={{
          showQuickJumper: true,
          showSizeChanger: true,
          pageSizeOptions: ['10', '25', '50', '100'],
          current: pageNum,
          pageSize,
          total,
          onChange: handleChangePagination,
          showTotal: (_total: number) => t('common.pagination.showTotal', { total: _total }),
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
