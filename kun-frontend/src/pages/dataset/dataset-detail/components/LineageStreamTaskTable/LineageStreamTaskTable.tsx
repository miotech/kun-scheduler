import { Link } from 'umi';
import React, { memo, useEffect, useMemo, useCallback } from 'react';
import { LineageDirection } from '@/services/lineage';
import useRedux from '@/hooks/useRedux';
import useI18n from '@/hooks/useI18n';
import {
  CheckCircleFilled,
  StopFilled,
  CloseCircleFilled,
} from '@ant-design/icons';

import { Table } from 'antd';
import { ColumnsType, TablePaginationConfig } from 'antd/lib/table';
import {
  LineageTask,
  DataQualityHistory,
} from '@/rematch/models/datasetDetail';
import styles from './LineageStreamTaskTable.less';

const colorMap = {
  warning: '#ff6336',
  green: '#9ac646',
  stop: '#526079',
};

interface Props {
  datasetId: string;
  direction: LineageDirection;
}

export default memo(function LineageStreamTaskTable({
  datasetId,
  direction,
}: Props) {
  const { selector, dispatch } = useRedux(state => state.datasetDetail);
  const t = useI18n();

  useEffect(() => {
    const pagination =
      direction === LineageDirection.UPSTREAM
        ? selector.upstreamLineageTaskListPagination
        : selector.downstreamLineageTaskListPagination;
    const lineageTasksParams = {
      datasetGid: datasetId,
      direction,
      ...pagination,
    };
    dispatch.datasetDetail.fetchLineageTasks(lineageTasksParams);
  }, [
    datasetId,
    direction,
    dispatch.datasetDetail,
    selector.downstreamLineageTaskListPagination,
    selector.upstreamLineageTaskListPagination,
  ]);

  const loading =
    direction === LineageDirection.UPSTREAM
      ? selector.fetchUpstreamLineageTaskListLoading
      : selector.fetchDownstreamLineageTaskListLoading;

  const columns: ColumnsType<LineageTask> = useMemo(
    () => [
      {
        key: 'taskName',
        dataIndex: 'taskName',
        title: t(`dataDetail.lineage.table.${direction}`),
        className: styles.nameColumn,
        width: 280,
        render: (taskName: string, record: LineageTask) => (
          <Link to={`/data-development/task-definition/${record.taskId}`}>
            {taskName}
          </Link>
        ),
      },
      {
        key: 'lastExecutedTime',
        dataIndex: 'lastExecutedTime',
        title: t('dataDetail.lineage.table.lastExecutedTime'),
        className: styles.nameColumn,
        width: 100,
      },
      {
        key: 'historyList',
        dataIndex: 'historyList',
        title: t('dataDetail.dataQualityTable.historyList'),
        render: (historyList: DataQualityHistory[]) => (
          <div className={styles.historyList}>
            {historyList?.map(history => {
              if (history === DataQualityHistory.SUCCESS) {
                return (
                  <CheckCircleFilled
                    className={styles.historyIcon}
                    style={{ color: colorMap.green }}
                  />
                );
              }
              if (history === DataQualityHistory.FAILED) {
                return (
                  <CloseCircleFilled
                    className={styles.historyIcon}
                    style={{ color: colorMap.warning }}
                  />
                );
              }
              if (history === DataQualityHistory.SKIPPED) {
                return (
                  <StopFilled
                    className={styles.historyIcon}
                    style={{ color: colorMap.stop }}
                  />
                );
              }
              return null;
            })}
          </div>
        ),
      },
    ],
    [direction, t],
  );

  const handleChangePagination = useCallback(
    (pageNumber: number, pageSize?: number) => {
      const paginationParamName =
        direction === LineageDirection.UPSTREAM
          ? 'upstreamLineageTaskListPagination'
          : 'downstreamLineageTaskListPagination';
      dispatch.datasetDetail.updateState({
        key: paginationParamName,
        value: {
          ...selector[paginationParamName],
          pageNumber,
          pageSize: pageSize || 25,
        },
      });
    },
    [direction, dispatch.datasetDetail, selector],
  );
  const handleChangePageSize = useCallback(
    (_pageNumber: number, pageSize: number) => {
      const paginationParamName =
        direction === LineageDirection.UPSTREAM
          ? 'upstreamLineageTaskListPagination'
          : 'downstreamLineageTaskListPagination';

      dispatch.datasetDetail.updateState({
        key: paginationParamName,
        value: {
          ...selector[paginationParamName],
          pageNumber: 1,
          pageSize: pageSize || 25,
        },
      });
    },
    [direction, dispatch.datasetDetail, selector],
  );

  const currentPagination =
    direction === LineageDirection.UPSTREAM
      ? selector.upstreamLineageTaskListPagination
      : selector.downstreamLineageTaskListPagination;

  const data =
    direction === LineageDirection.UPSTREAM
      ? selector.upstreamLineageTaskList
      : selector.downstreamLineageTaskList;

  const pagination: TablePaginationConfig = useMemo(
    () => ({
      size: 'small',
      total: currentPagination.totalCount,
      showSizeChanger: true,
      showQuickJumper: true,
      onChange: handleChangePagination,
      onShowSizeChange: handleChangePageSize,
      pageSize: currentPagination.pageSize,
      pageSizeOptions: ['25', '50', '100', '200'],
    }),
    [
      handleChangePageSize,
      handleChangePagination,
      currentPagination.pageSize,
      currentPagination.totalCount,
    ],
  );

  return (
    <Table
      rowKey="taskId"
      loading={loading}
      className={styles.lineageStreamTaskTable}
      columns={columns}
      dataSource={data || undefined}
      pagination={pagination}
      onHeaderRow={() => ({
        className: styles.header,
      })}
      size="small"
    />
  );
});
