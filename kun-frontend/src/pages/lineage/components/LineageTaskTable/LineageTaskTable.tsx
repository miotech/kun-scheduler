import { Link } from 'umi';
import React, { memo, useEffect, useMemo, useState } from 'react';
import useI18n from '@/hooks/useI18n';
import { CheckCircleFilled, CloseCircleFilled } from '@ant-design/icons';
import { dayjs } from '@/utils/datetime-utils';

import { Table } from 'antd';
import { ColumnsType, TablePaginationConfig } from 'antd/lib/table';
import { LineageTask } from '@/rematch/models/datasetDetail';
import useBackPath from '@/hooks/useBackPath';
import { LineageHistoryStatus } from '@/definitions/Lineage.type';
import { getTaskDefinitionIdByWorkflowIds } from '@/services/task-deployments/deployed-tasks';
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

  const [taskIdToDefIdMapIsLoading, setTaskIdToDefIdMapIsLoading] = useState<
    boolean
  >(false);
  const [taskIdToDefIdMap, setTaskIdToDefIdMap] = useState<
    Record<string, string | null>
  >({});

  useEffect(() => {
    async function fetchMap(workflowTaskIds: string[]) {
      setTaskIdToDefIdMapIsLoading(true);
      try {
        const defMap = await getTaskDefinitionIdByWorkflowIds(workflowTaskIds);
        if (Object.keys(defMap).length > 0) {
          setTaskIdToDefIdMap(defMap);
        }
      } finally {
        setTaskIdToDefIdMapIsLoading(false);
      }
    }
    if (data && data.length) {
      fetchMap(data.map(dt => `${dt.taskId}`));
    }
  }, [data]);

  const columns: ColumnsType<LineageTask> = useMemo(
    () => [
      {
        key: 'taskName',
        dataIndex: 'taskName',
        title: taskColumnName,
        className: styles.nameColumn,
        width: 140,
        ellipsis: true,
        render: (taskName: string, record: LineageTask) => {
          if (taskIdToDefIdMap[record.taskId] != null) {
            return (
              <Link
                className={styles.taskName}
                to={getBackPath(
                  `/data-development/task-definition/${
                    taskIdToDefIdMap[record.taskId]
                  }`,
                )}
              >
                {taskName}
              </Link>
            );
          }
          // else
          return <span>{taskName}</span>;
        },
      },
      {
        key: 'lastExecutedTime',
        dataIndex: 'lastExecutedTime',
        title: t('dataDetail.lineage.table.lastExecutedTime'),
        className: styles.nameColumn,
        width: 120,
        render: (txt: string) =>
          txt != null ? dayjs(txt).format('YYYY-MM-DD HH:mm:ss') : '-',
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
    [getBackPath, t, taskColumnName, taskIdToDefIdMap],
  );

  return (
    <Table
      rowKey="taskId"
      loading={loading || taskIdToDefIdMapIsLoading}
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
