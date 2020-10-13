import React from 'react';
import c from 'clsx';

import useI18n from '@/hooks/useI18n';

import DateRangeOption from './DateRangeOption';
import css from './DateRangeRadioFilter.less';

interface Props {
  startDate?: null | number;
  endDate?: null | number;
  defaultStartDate?: null | number;
  defaultEndDate?: null | number;
  dateOptions?: Array<{ count: number; unit: string; title: string }>;
  showAll?: boolean;
  onChange: (payload: {
    startDate: number | null;
    endDate: number | null;
  }) => void;
}

function DateRangeRadioFilter({
  startDate = null,
  endDate = null,
  defaultStartDate = null,
  defaultEndDate = null,
  dateOptions = [
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
  showAll = true,
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
          {t(`common.dateRange.${dateOption.title}`)}
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
          {t('common.dateRange.all')}
        </span>
      )}
    </div>
  );
}

export default DateRangeRadioFilter;
