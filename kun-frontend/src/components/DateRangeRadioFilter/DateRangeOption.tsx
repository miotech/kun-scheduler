import React, { memo, useMemo } from 'react';
import moment, { Moment } from 'moment';
import c from 'classnames';
import './DateRangeOption.less';

interface Props {
  startDate?: number | null;
  endDate?: number | null;
  targetStartDateGetter: (start: Moment) => number;
  onClick: (targetStartDate: number, targetEndDate: number) => void;
  children: React.ReactNode;
}

export default memo(function DateRangeOption({
  startDate = null,
  endDate = null,
  targetStartDateGetter,
  onClick,
  children,
}: Props) {
  const targetStartDate = useMemo(() => targetStartDateGetter(moment()), [
    targetStartDateGetter,
  ]);
  const targetEndDate = useMemo(() => moment().valueOf(), []);

  const isActive = useMemo(
    () =>
      moment(targetStartDate)
        .startOf('day')
        .valueOf() ===
        moment(startDate)
          .startOf('day')
          .valueOf() &&
      moment(targetEndDate)
        .startOf('day')
        .valueOf() ===
        moment(endDate)
          .startOf('day')
          .valueOf(),
    [endDate, startDate, targetEndDate, targetStartDate],
  );

  return (
    <span
      className={c('filters__range-option', {
        active: isActive,
      })}
      onClick={() => {
        onClick(targetStartDate, targetEndDate);
      }}
    >
      {children}
    </span>
  );
});
