import { Link } from 'umi';
import React, { memo, useEffect, useState, useMemo } from 'react';
import { Divider } from 'antd';
import { TablePaginationConfig } from 'antd/lib/table';
import { fetchLineageRelatedTasksService } from '@/services/lineage';
import { LineageTask } from '@/rematch/models/datasetDetail';
import useI18n from '@/hooks/useI18n';
import useBackPath from '@/hooks/useBackPath';
import Iconfont from '@/components/Iconfont';

import LineageTaskTable from '../LineageTaskTable/LineageTaskTable';
import styles from './RelatedTaskCard.less';

interface Props {
  sourceDatasetId: string;
  destDatasetId: string;
  sourceDatasetName: string;
  destDatasetName: string;
}

export default memo(function RelatedTaskCard({
  sourceDatasetId,
  sourceDatasetName,
  destDatasetId,
  destDatasetName,
}: Props) {
  const t = useI18n();

  const { getBackPath } = useBackPath();

  const [taskList, setTaskList] = useState<LineageTask[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  useEffect(() => {
    const fetchFunc = async () => {
      setLoading(true);
      try {
        const taskListResp = await fetchLineageRelatedTasksService({
          sourceDatasetGid: sourceDatasetId,
          destDatasetGid: destDatasetId,
        });
        setTaskList(taskListResp.tasks);
      } catch (e) {
        // do nothing
      } finally {
        setLoading(false);
      }
    };
    fetchFunc();
  }, [destDatasetId, sourceDatasetId]);

  const pagination: TablePaginationConfig = useMemo(
    () => ({
      size: 'small',
      showSizeChanger: true,
      showQuickJumper: true,
      defaultPageSize: 10,
      pageSizeOptions: ['10', '25', '50', '100', '200'],
    }),
    [],
  );

  return (
    <div className={styles.RelatedTaskCard}>
      <div className={styles.title}>{t('lineage.relatedFlow.title')}</div>
      <div className={styles.relatedRow}>
        <span className={styles.datasetBlock}>
          <Iconfont type="column" gapRight={4} />
          <Link
            to={getBackPath(`/data-discovery/dataset/${sourceDatasetId}`)}
            className={styles.datasetName}
          >
            {sourceDatasetName}
          </Link>
        </span>

        <span className={styles.datasetBlock}>
          <div className={styles.arrowBody} />
          <div className={styles.arrowHead} />
        </span>

        <span className={styles.datasetBlock}>
          <Iconfont type="column" gapRight={4} />
          <Link
            to={getBackPath(`/data-discovery/dataset/${destDatasetId}`)}
            className={styles.datasetName}
          >
            {destDatasetName}
          </Link>
        </span>
      </div>

      <Divider className={styles.divider} />

      <LineageTaskTable
        loading={loading}
        data={taskList}
        pagination={pagination}
        taskColumnName={t('lineage.relatedFlow.relatedTask')}
      />
    </div>
  );
});
