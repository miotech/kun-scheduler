import React, { memo, useMemo } from 'react';
import c from 'clsx';
import dayjs from 'dayjs';
import { Link } from 'umi';
import numeral from 'numeral';
import isNumber from 'lodash/isNumber';
import isNil from 'lodash/isNil';
import useI18n from '@/hooks/useI18n';
import Iconfont from '@/components/Iconfont';
import {
  CardPort, OnCollapseEventHandler, OnExpandEventHandler
} from '@/components/LineageDiagram/DatasetNodeCard/CardPort';

import { Dataset } from '@/definitions/Dataset.type';

import './DatasetNodeCard.less';

export type PortStateType = 'hidden' | 'collapsed' | 'expanded' | 'loading';

interface OwnProps {
  /** the status of dataset node */
  state?: 'default' | 'selected' | 'faded' | 'highlighted';
  data?: Dataset | null;
  rowCount?: number;
  lastUpdateTime?: Date | number | string;
  onClick?: (ev: React.MouseEvent) => any;
  leftPortState?: PortStateType;
  rightPortState?: PortStateType;
  onExpandLeft?: OnExpandEventHandler;
  onCollapseLeft?: OnCollapseEventHandler;
  onExpandRight?: OnExpandEventHandler;
  onCollapseRight?: OnCollapseEventHandler;
  /** use native link instead of umi <Link />. Only for demo purpose */
  useNativeLink?: boolean;
}

export type DatasetNodeCardProps = OwnProps & React.ComponentProps<'div'>;

const clsPrefix = 'lineage-dataset-node-card';

function formatRowCount(rowCount: number) {
  if (rowCount > 1000000) {
    return numeral(rowCount).format('0,0.00a');
  }
  // else
  return numeral(rowCount).format('0,0');
}

export const DatasetNodeCard: React.FC<DatasetNodeCardProps> = memo(function DatasetNodeCard(props) {
  const {
    state = 'default',
    className,
    data,
    onClick,
    rowCount,
    lastUpdateTime,
    useNativeLink = false,
    leftPortState = 'hidden',
    rightPortState = 'hidden',
    onExpandLeft,
    onCollapseLeft,
    onExpandRight,
    onCollapseRight,
  } = props;

  const t = useI18n();

  const titleLink = useMemo(() => {
    if (!data) {
      return <></>;
    }
    // else
    return (
      <h1 className={`${clsPrefix}__title-link`}>
        {useNativeLink ? (
          <a href="#">
            <Iconfont type="column" ariaHidden />
            <span data-label="dataset-name" title={data.name}>
              {data.name}
            </span>
          </a>
        ) : (
          <Link to="#">
            <Iconfont type="column" ariaHidden />
            <span data-label="dataset-name" title={data.name}>
              {data.name}
            </span>
          </Link>
        )}
      </h1>
    );
  }, [
    data,
    useNativeLink,
  ]);

  return (
    <div
      className={c(`${clsPrefix}`, `${clsPrefix}--${state}`, className)}
      onClick={onClick}
      aria-label="lineage-dataset-node-card"
    >
      <div className={`${clsPrefix}__heading`}>
        <section className={`${clsPrefix}__left-content`}>
          {/* Title */}
          {titleLink}
          {/* TODO: figure out what content is on the bottom? */}
          <p className={`${clsPrefix}__description`} aria-label="description" title={data?.description}>
            {data?.description || 'no description'}
          </p>
        </section>
        <section className={`${clsPrefix}__right-content`}>
          {/* Row count */}
          <dl data-label="row-count-wrapper">
            <dt data-label="row-count-label">
              {t('lineage.rowCount')}
            </dt>
            <dd data-label="row-count-value">
              {isNumber(rowCount) ? formatRowCount(rowCount) : 'N/A'}
            </dd>
          </dl>
          {/* Last update time */}
          <div data-label="update-time-wrapper">
            <span data-label="last-update-time">
              {isNil(lastUpdateTime) ? '-' : dayjs(lastUpdateTime).format('YYYY-MM-DD HH:mm:ss')}
            </span>
          </div>
        </section>
      </div>
      <div className={`${clsPrefix}__details`}>
        <section className={`${clsPrefix}__left-content`}>
          <dl>
            <dt className={`${clsPrefix}__def-title`}>{t('lineage.dataSourceName')}</dt>
            <dd className={`${clsPrefix}__def-desc`}>{data?.datasource ?? 'N/A'}</dd>
          </dl>
        </section>
        <section className={`${clsPrefix}__right-content`}>
          <dl>
            <dt className={`${clsPrefix}__def-title`}>{t('lineage.dataSourceType')}</dt>
            <dd className={`${clsPrefix}__def-desc`}>{data?.type ?? 'N/A'}</dd>
          </dl>
        </section>
      </div>
      {/* Left port (from upstream) */}
      <CardPort
        className="left-port"
        portState={leftPortState}
        onExpand={onExpandLeft}
        onCollapse={onCollapseLeft}
      />
      {/* Right port (to downstream) */}
      <CardPort
        className="right-port"
        portState={rightPortState}
        onExpand={onExpandRight}
        onCollapse={onCollapseRight}
      />
    </div>
  );
});
