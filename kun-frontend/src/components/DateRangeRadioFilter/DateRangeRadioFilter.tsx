import React from 'react';
import c from 'classnames';

import useI18n from '@/hooks/useI18n';

import DateRangeOption from './DateRangeOption';
import css from './DateRangeRadioFilter.less';

interface Props {
  startDate: null | number;
  endDate: null | number;
  defaultStartDate: null | number;
  defaultEndDate: null | number;
  dateOptions: Array<{ count: number; unit: string; title: string }>;
  showAll: boolean;
  onChange: (payload: {
    startDate: number | null;
    endDate: number | null;
  }) => void;
}

function DateRangeRadioFilter({
  startDate,
  endDate,
  defaultStartDate,
  defaultEndDate,
  dateOptions,
  showAll,
  onChange,
}: Props) {
  const t = useI18n();

  return (
    <div className={css.container}>
      {dateOptions.map(dateOption => (
        <DateRangeOption
          key={dateOption.title}
          startDate={startDate}
          endDate={endDate}
          targetStartDateGetter={(start: any) =>
            start.subtract(dateOption.count, dateOption.unit).valueOf()
          }
          onClick={(targetStartDate: any, targetEndDate: any) => {
            onChange({
              startDate: targetStartDate,
              endDate: targetEndDate,
            });
          }}
        >
          {t(`commen.dateRange.${dateOption.title}`)}
        </DateRangeOption>
      ))}
      {showAll && (
        <span
          className={c(css.option, {
            [css.isActive]:
              (defaultStartDate === startDate && defaultEndDate === endDate) ||
              (!startDate && !endDate && !defaultStartDate && !defaultEndDate),
          })}
          onClick={() => {
            onChange({
              startDate: defaultStartDate,
              endDate: defaultEndDate,
            });
          }}
        >
          {t('commen.dateRange.all')}
        </span>
      )}
    </div>
  );
}

DateRangeRadioFilter.defaultProps = {
  showAll: true,
  defaultStartDate: null,
  defaultEndDate: null,
  startDate: null,
  endDate: null,
  dateOptions: [
    {
      count: 1,
      unit: 'day',
      title: 'last24hours',
    },
    {
      count: 1,
      unit: 'week',
      title: 'lastWeek',
    },
    {
      count: 1,
      unit: 'month',
      title: 'lastMonth',
    },
  ],
};

export default DateRangeRadioFilter;
