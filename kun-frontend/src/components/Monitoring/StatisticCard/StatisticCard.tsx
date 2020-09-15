import React, { memo, ReactText } from 'react';
import numeral from 'numeral';
import c from 'clsx';

import './StatisticCard.global.less';

interface OwnProps {
  /** title of this card component */
  title?: ReactText;
  /** statistic value shown at the bottom of card */
  value?: number;
  /** theme color for statistic value */
  textTheme?: 'default' | 'success' | 'failed' | 'running';
}

export type StatisticCardProps = OwnProps & React.ComponentProps<'div'>;

export const StatisticCard: React.FC<StatisticCardProps> = memo((props) => {
  const {
    title,
    value,
    className,
    textTheme = 'default',
    ...restProps
  } = props;

  return (
    <div
      className={c('monitoring-statistic-card', className)}
      aria-label="monitoring-statistic-card"
      {...restProps}
    >
      <h3 className="monitoring-statistic-card__title">
        {title}
      </h3>
      <div className={c('monitoring-statistic-card__value', `monitoring-statistic-card__value--${textTheme}`)}>
        {numeral(value).format('0,0')}
      </div>
    </div>
  );
});
