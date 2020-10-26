import React, { memo, useEffect, useMemo } from 'react';
import { LineageDirection } from '@/services/lineage';
import useRedux from '@/hooks/useRedux';
import { TablePaginationConfig } from 'antd/lib/table';

import useI18n from '@/hooks/useI18n';

import LineageTaskTable from '@/pages/lineage/components/LineageTaskTable/LineageTaskTable';

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
    const lineageTasksParams = {
      datasetGid: datasetId,
      direction,
    };
    dispatch.datasetDetail.fetchLineageTasks(lineageTasksParams);
  }, [datasetId, direction, dispatch.datasetDetail]);

  const loading =
    direction === LineageDirection.UPSTREAM
      ? selector.fetchUpstreamLineageTaskListLoading
      : selector.fetchDownstreamLineageTaskListLoading;

  const data =
    direction === LineageDirection.UPSTREAM
      ? selector.upstreamLineageTaskList
      : selector.downstreamLineageTaskList;

  const pagination: TablePaginationConfig = useMemo(
    () => ({
      size: 'small',
      showSizeChanger: true,
      showQuickJumper: true,
      defaultPageSize: 5,
      pageSizeOptions: ['5', '10', '20'],
    }),
    [],
  );

  return (
    <LineageTaskTable
      loading={loading}
      data={data || []}
      pagination={pagination}
      taskColumnName={t(`dataDetail.lineage.table.${direction}`)}
    />
  );
});
