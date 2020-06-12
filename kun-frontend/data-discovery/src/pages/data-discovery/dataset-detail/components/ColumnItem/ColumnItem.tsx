import React, { memo, useCallback } from 'react';
import numeral from 'numeral';
import { message } from 'antd';
import { watermarkFormatter } from '@/utils';
import useRedux from '@/hooks/useRedux';
import useI18n from '@/hooks/useI18n';

import { Column } from '@/rematch/models/datasetDetail';

import DescriptionInput from '../DescriptionInput/DescriptionInput';

import styles from './ColumnItem.less';

interface Props {
  column: Column;
  onFinishUpdate?: () => void;
}

export default memo(function ColumnItem({ column, onFinishUpdate }: Props) {
  const { dispatch } = useRedux(state => state.datasetDetail);
  const t = useI18n();

  const handleChangeDescription = useCallback(
    v => {
      const diss = message.loading(t('common.loading'), 0);
      dispatch.datasetDetail
        .updateColumn({ id: column.id, description: v })
        .then(resp => {
          if (resp && onFinishUpdate) {
            onFinishUpdate();
          }
          diss();
        });
    },
    [column.id, dispatch.datasetDetail, onFinishUpdate, t],
  );

  return (
    <div className={styles.column}>
      <div className={styles.titleRow}>
        <span className={styles.columnName}>{column.name}</span>
        <span className={styles.watermark}>
          {watermarkFormatter(column.high_watermark.time)}
        </span>
      </div>

      <div className={styles.contentArea}>
        <div className={styles.notNullCount}>
          <div className={styles.titleItem}>
            {t('dataDetail.column.notNullCount')}
          </div>
          <div className={styles.contentItem}>{column.not_null_count}</div>
        </div>

        <div className={styles.notNullPer}>
          <div className={styles.titleItem}>
            {t('dataDetail.column.notNullPer')}
          </div>
          <div className={styles.contentItem}>
            {numeral(column.not_null_percentage).format('0.00%')}
          </div>
        </div>

        <div className={styles.type}>
          <div className={styles.titleItem}>{t('dataDetail.column.type')}</div>
          <div className={styles.contentItem}>{column.type}</div>
        </div>

        <div className={styles.description}>
          <DescriptionInput
            value={column.description}
            onChange={handleChangeDescription}
          />
        </div>
      </div>
    </div>
  );
});
