import React, { memo, ReactText, useMemo } from 'react';
import c from 'clsx';
import numeral from 'numeral';
import isNil from 'lodash/isNil';
import isFinite from 'lodash/isFinite';
import isString from 'lodash/isString';
import isNaN from 'lodash/isNaN';
import { Tooltip, Skeleton } from 'antd';
import { InfoCircleOutlined } from '@ant-design/icons';

import './RatioCard.global.less';

// Status of current statistic result which decides the appearance of component (text colors, usually).
export type RatioCardStatus = 'healthy' | 'noticeable' | 'warning' | 'auto' | 'unknown';

export interface RatioCardProps extends React.ComponentProps<'div'> {
  /** Ratio card title text */
  title?: string;
  /** Tooltip content on the right side of title. Will not display if nullish value provided. */
  titleTooltip?: ReactText | null;
  /** numerator content (usually fulfilled cases number) */
  numerator?: ReactText;
  /** denominator content (usually total number) */
  denominator?: ReactText;
  /** display ratio at right bottom corner (default true) */
  displayRatio?: boolean;
  /** status */
  status?: RatioCardStatus;
  /** Loading state */
  loading?: boolean;
}

/**
 * Ratio card component for monitoring dashboard
 */
export const RatioCard: React.FC<RatioCardProps> = memo(function RatioCard(props) {
  const {
    title,
    titleTooltip,
    numerator,
    denominator,
    displayRatio,
    status = 'auto',
    loading,
    ...restProps
  } = props;

  const tooltip = useMemo(() => isNil(titleTooltip) ? <></> : (
    <Tooltip title={titleTooltip}>
      <InfoCircleOutlined className="monitoring-ratio-card__tooltip-icon" />
    </Tooltip>
  ), [
    titleTooltip,
  ]);

  const numeratorStatusClsSuffix = useMemo(() => {
    switch (status) {
      case 'auto':
        if (isNaN(Number(`${numerator}`)) ||
          isNaN(Number(`${denominator}`)) ||
          Number(`${denominator}`) === 0) {
          return 'unknown';
        }
        // else
        if (Number(numerator as string) / Number(denominator as string) >= 0.8) {
          return 'healthy';
        }
        if (Number(numerator as string) / Number(denominator as string) >= 0.6) {
          return 'noticeable';
        }
        // else
        return 'warning';
      default:
        return status;
    }
  }, [denominator, numerator, status]);

  const percentage = useMemo(() => {
    if (isNil(denominator) || isNil(numerator)) {
      return 'N/A';
    }
    const numDenominator: number = isString(denominator) ? Number(denominator) : denominator;
    const numNumerator: number = isString(numerator) ? Number(numerator) : numerator;

    const percentageNum = (numNumerator / numDenominator) * 100;
    if (isNaN(percentageNum) || (!isFinite(percentageNum))) {
      return 'N/A';
    }
    return `${percentageNum.toFixed(2)}%`;
  }, [
    denominator,
    numerator,
  ]);

  const renderMetricNumbers = () => {
    if (loading) {
      return (
        <div
          className="monitoring-ratio-card__statistic--loading"
          aria-label="ratio-card-statistic-loading"
        >
          <Skeleton
            active
            title
            paragraph={false}
          />
        </div>
      );
    }
    // else
    return (
      <div className="monitoring-ratio-card__metrics">
        {/* counts */}
        <div
          className="monitoring-ratio-card__statistic"
          aria-label="ratio-card-statistic"
        >
          <span
            className={c(
              'monitoring-ratio-card__statistic__numerator',
              `monitoring-ratio-card__statistic__numerator--${numeratorStatusClsSuffix}`,
            )}
            aria-label="numerator"
          >
            {numeral(numerator).format('0,0')}
          </span>
          <span className="monitoring-ratio-card__statistic__dash" aria-label="dash">/</span>
          <span className="monitoring-ratio-card__statistic__denominator" aria-label="denominator">
            {numeral(denominator).format('0,0')}
          </span>
        </div>
        {/* percentage */}
        <div
          className="monitoring-ratio-card__ratio-percentage"
          aria-label="ratio-card-percentage"
        >
          {percentage}
        </div>
      </div>
    );
  };

  return (
    <div
      {...restProps}
      className={c('monitoring-ratio-card', restProps.className)}
    >
      {/* Title */}
      <h3
        className="monitoring-ratio-card__title"
        aria-label="ratio-card-title"
      >
        <span>{title}</span>
        {tooltip}
      </h3>
      {/* Metric numbers */}
      {renderMetricNumbers()}
    </div>
  );
});
