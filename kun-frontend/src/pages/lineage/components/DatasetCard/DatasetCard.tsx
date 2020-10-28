import { useRouteMatch, Link } from 'umi';
import React, { memo, useEffect, useMemo } from 'react';
import moment from 'moment';
import { Spin, Divider } from 'antd';
import { TablePaginationConfig } from 'antd/lib/table';
import useRedux from '@/hooks/useRedux';
import { LineageDirection } from '@/services/lineage';
import Iconfont from '@/components/Iconfont';
import useI18n from '@/hooks/useI18n';
import useBackPath from '@/hooks/useBackPath';

import LineageTaskTable from '../LineageTaskTable/LineageTaskTable';

import styles from './DatasetCard.less';

interface Props {
  datasetId: string;
  isExpanded: boolean;
}

export default memo(function DatasetCard({ datasetId, isExpanded }: Props) {
  const match = useRouteMatch<{ datasetId: string }>();

  const { getBackPath } = useBackPath();

  const t = useI18n();
  const { selector, dispatch } = useRedux(state => state.lineage);

  const {
    activeDatasetDetail,
    activeDatasetDetailLoading,
    activeUpstreamLineageTaskList,
    activeDownstreamLineageTaskList,
    activeFetchUpstreamLineageTaskListLoading,
    activeFetchDownstreamLineageTaskListLoading,
  } = selector;

  useEffect(() => {
    if (isExpanded && datasetId) {
      dispatch.lineage.fetchDatasetDetail(datasetId);
      dispatch.lineage.fetchLineageTasks({
        datasetGid: datasetId,
        direction: LineageDirection.UPSTREAM,
      });
      dispatch.lineage.fetchLineageTasks({
        datasetGid: datasetId,
        direction: LineageDirection.DOWNSTREAM,
      });
    }
  }, [datasetId, dispatch.lineage, isExpanded]);

  const upPagination: TablePaginationConfig = useMemo(
    () => ({
      size: 'small',
      showSizeChanger: true,
      showQuickJumper: true,
      defaultPageSize: 10,
      pageSizeOptions: ['10', '25', '50', '100', '200'],
    }),
    [],
  );

  const downPagination: TablePaginationConfig = useMemo(
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
    <div className={styles.DatasetCard}>
      <Spin spinning={activeDatasetDetailLoading}>
        <div className={styles.titleRow}>
          <div className={styles.title}>
            <Iconfont type="column" />
            <Link
              to={getBackPath(
                `/data-discovery/dataset/${match.params.datasetId}`,
              )}
              className={styles.datasetName}
            >
              {activeDatasetDetail?.name}
            </Link>
          </div>
          <div className={styles.timeAndCount}>
            <div>
              {`${t(
                'dataDetail.baseItem.title.rowCount',
              )}: ${activeDatasetDetail?.rowCount || ''}`}
            </div>
            <div>
              {activeDatasetDetail?.highWatermark?.time &&
                moment(Number(activeDatasetDetail?.highWatermark?.time)).format(
                  'YYYY-MM-DD HH:mm:ss',
                )}
            </div>
          </div>
        </div>
        <div className={styles.datasetInfoRow}>
          <div className={styles.infoBlock}>
            <div className={styles.infoTitle}>{t('lineage.databaseName')}</div>
            <div className={styles.infoContent}>
              {activeDatasetDetail?.database}
            </div>
          </div>
          <div className={styles.infoBlock}>
            <div className={styles.infoTitle}>
              {t('lineage.dataSourceName')}
            </div>
            <div className={styles.infoContent}>
              {activeDatasetDetail?.datasource}
            </div>
          </div>
          <div className={styles.infoBlock}>
            <div className={styles.infoTitle}>
              {t('lineage.dataSourceType')}
            </div>
            <div className={styles.infoContent}>
              {activeDatasetDetail?.type}
            </div>
          </div>
        </div>
      </Spin>

      <Divider className={styles.divider} />

      <LineageTaskTable
        loading={activeFetchUpstreamLineageTaskListLoading}
        data={activeUpstreamLineageTaskList}
        pagination={upPagination}
        taskColumnName={t(
          `dataDetail.lineage.table.${LineageDirection.UPSTREAM}`,
        )}
      />
      <LineageTaskTable
        loading={activeFetchDownstreamLineageTaskListLoading}
        data={activeDownstreamLineageTaskList}
        pagination={downPagination}
        taskColumnName={t(
          `dataDetail.lineage.table.${LineageDirection.DOWNSTREAM}`,
        )}
      />
    </div>
  );
});
