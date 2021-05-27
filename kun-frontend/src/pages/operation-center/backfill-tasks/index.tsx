import React, { memo, SyntheticEvent, useEffect } from 'react';
import { useUnmount } from 'ahooks';
import useRedux from '@/hooks/useRedux';
import useDebouncedUpdateEffect from '@/hooks/useDebouncedUpdateEffect';

import { Card } from 'antd';
import { ViewFilters } from '@/pages/operation-center/backfill-tasks/components/ViewFilters';
import { BackfillTable } from '@/pages/operation-center/backfill-tasks/components/BackfillTable';

import { BackfillModelState } from '@/rematch/models/operationCenter/backfillTasks/model-state';
import { Backfill } from '@/definitions/Backfill.type';

import css from './index.less';

interface OwnProps {}

type Props = OwnProps;

export const BackfillTaskView: React.FC<Props> = memo(function BackfillTaskView() {
  const {
    dispatch,
    selector: { filters, total, tableIsLoading, tableData },
  } = useRedux<{
    filters: BackfillModelState['filters'];
    total: number;
    tableData: Backfill[];
    tableIsLoading: boolean;
  }>(s => ({
    filters: s.backfillTasks.filters,
    total: s.backfillTasks.total,
    tableIsLoading: s.backfillTasks.tableIsLoading,
    tableData: s.backfillTasks.tableData,
  }));

  function resetToFirstPage() {
    dispatch.backfillTasks.updateFilter({
      pageNum: 1,
    });
  }

  function fetchData(pageNum?: number) {
    dispatch.backfillTasks.loadBackfillData({
      pageNumber: pageNum ?? filters.pageNum,
      pageSize: filters.pageSize,
      name: (filters.keyword || '').trim() ? (filters.keyword || '').trim() : undefined,
      creatorIds: filters.creatorId == null ? undefined : [filters.creatorId],
      timeRngStart:
        filters.startTimeRng == null
          ? undefined
          : filters.startTimeRng
              .hour(0)
              .minute(0)
              .second(0)
              .toISOString(),
      timeRngEnd:
        filters.endTimeRng == null
          ? undefined
          : filters.endTimeRng
              .hour(23)
              .minute(59)
              .second(59)
              .toISOString(),
    });
  }

  useEffect(() => {
    fetchData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filters.pageNum, filters.pageSize]);

  useDebouncedUpdateEffect(
    () => {
      resetToFirstPage();
      fetchData(1);
    },
    [filters.keyword],
    {
      wait: 1000,
    },
  );

  useEffect(() => {
    resetToFirstPage();
    fetchData(1);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filters.creatorId, filters.startTimeRng, filters.endTimeRng]);

  useUnmount(() => {
    dispatch.backfillTasks.resetAll();
  });

  return (
    <div id="backfill-task-view">
      <main id="backfill-task-view-main-content" className={css.MainContent}>
        <ViewFilters
          filters={filters}
          onClickRefresh={(ev: SyntheticEvent) => {
            if (ev) {
              ev.persist();
            }
            fetchData();
          }}
          refreshBtnLoading={tableIsLoading}
        />
        <Card className={css.CardContainer}>
          <BackfillTable
            data={tableData}
            pageNum={filters.pageNum}
            pageSize={filters.pageSize}
            total={total}
            loading={tableIsLoading}
          />
        </Card>
      </main>
    </div>
  );
});

export default BackfillTaskView;
