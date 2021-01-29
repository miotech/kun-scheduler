import React, { memo, SyntheticEvent, useCallback } from 'react';
import { Input, DatePicker, Button } from 'antd';
import { UserSelect } from '@/components/UserSelect';
import { ReloadOutlined } from '@ant-design/icons';

import useI18n from '@/hooks/useI18n';

import { Moment } from 'moment';
import useRedux from '@/hooks/useRedux';
import css from './ViewFilters.less';

interface OwnProps {
  filters: {
    keyword: string;
    creatorId: string | null;
    startTimeRng: Moment | null;
    endTimeRng: Moment | null;
  };
  onClickRefresh: (ev: SyntheticEvent) => any;
  refreshBtnLoading?: boolean;
}

type Props = OwnProps;

const { RangePicker } = DatePicker;

export const ViewFilters: React.FC<Props> = memo(function ViewFilters(props) {
  const t = useI18n();

  const { filters, onClickRefresh, refreshBtnLoading = false } = props;

  const { dispatch } = useRedux(() => ({}));

  const handleKeywordChange = useCallback(function handleKeywordChange(ev) {
    dispatch.backfillTasks.updateFilter({
      keyword: ev.target.value,
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleCreatorIdChange = useCallback(
    function handleCreatorIdChange(userId: string) {
      dispatch.backfillTasks.updateFilter({
        creatorId: userId || null,
      });
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [],
  );

  const handleTimeRangeFilterChange = useCallback(
    function handleTimeRangeFilterChange(value: [Moment, Moment] | null) {
      const [t1, t2] = value || [null, null];
      dispatch.backfillTasks.updateFilter({
        startTimeRng: t1 || null,
        endTimeRng: t2 || null,
      });
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [],
  );

  return (
    <nav className={css.ViewFilters}>
      {/* backfill group name search */}
      <div className={css.SearchInputWrapper}>
        <Input.Search
          placeholder={t('operationCenter.backfill.filters.search.placeholder')}
          value={filters.keyword}
          onChange={handleKeywordChange}
          onClick={() => {
            if (handleKeywordChange) {
              handleKeywordChange({
                target: {
                  value: filters.keyword,
                },
              });
            }
          }}
        />
      </div>
      {/* backfill creators filter */}
      <div className={css.UserSelectWrapper}>
        <UserSelect
          allowClear
          value={filters.creatorId || undefined}
          onChange={handleCreatorIdChange as any}
          placeholder={t(
            'operationCenter.backfill.filters.userSelect.placeholder',
          )}
        />
      </div>
      {/* backfill created time range */}
      <div className={css.RangePickerWrapper}>
        <RangePicker
          format="YYYY/MM/DD"
          onChange={handleTimeRangeFilterChange as any}
        />
      </div>
      <div className={css.ReloadBtnWrapper}>
        <Button
          icon={<ReloadOutlined />}
          onClick={onClickRefresh}
          loading={refreshBtnLoading}
        >
          {t('common.refresh')}
        </Button>
      </div>
    </nav>
  );
});
