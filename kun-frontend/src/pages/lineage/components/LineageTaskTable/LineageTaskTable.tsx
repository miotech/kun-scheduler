import { Link } from 'umi';
import React, { memo, useMemo } from 'react';
import useI18n from '@/hooks/useI18n';
import { CheckCircleFilled, CloseCircleFilled } from '@ant-design/icons';

import { Table } from 'antd';
import { ColumnsType, TablePaginationConfig } from 'antd/lib/table';
import { LineageTask } from '@/rematch/models/datasetDetail';
import useBackPath from '@/hooks/useBackPath';
import { LineageHistoryStatus } from '@/definitions/Lineage.type';
import styles from './LineageTaskTable.less';

const colorMap = {
  warning: '#ff6336',
  green: '#9ac646',
  stop: '#526079',
};

interface Props {
  data: LineageTask[] | null;
  taskColumnName: string;
  pagination: TablePaginationConfig;
  loading: boolean;
}

export default memo(function LineageTaskTable({
  data,
  taskColumnName,
  pagination,
  loading,
}: Props) {
  const t = useI18n();

  const { getBackPath } = useBackPath();

  const columns: ColumnsType<LineageTask> = useMemo(
    () => [
      {
        key: 'taskName',
        dataIndex: 'taskName',
        title: taskColumnName,
        className: styles.nameColumn,
        width: 90,
        render: (taskName: string, record: LineageTask) => (
          <Link
            className={styles.taskName}
            to={getBackPath(
              `/data-development/task-definition/${record.taskId}`,
            )}
          >
            {taskName}
          </Link>
        ),
      },
      {
        key: 'lastExecutedTime',
        dataIndex: 'lastExecutedTime',
        title: t('dataDetail.lineage.table.lastExecutedTime'),
        className: styles.nameColumn,
        width: 140,
      },
      {
        key: 'historyList',
        dataIndex: 'historyList',
        title: t('dataDetail.dataQualityTable.historyList'),
        render: (historyList: LineageHistoryStatus[]) => (
          <div className={styles.historyList}>
            {historyList?.map(history => {
              if (history === LineageHistoryStatus.SUCCESS) {
                return (
                  <CheckCircleFilled
                    className={styles.historyIcon}
                    style={{ color: colorMap.green }}
                  />
                );
              }
              if (history === LineageHistoryStatus.FAILED) {
                return (
                  <CloseCircleFilled
                    className={styles.historyIcon}
                    style={{ color: colorMap.warning }}
                  />
                );
              }
              return null;
            })}
          </div>
        ),
        width: 140,
      },
    ],
    [getBackPath, t, taskColumnName],
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
