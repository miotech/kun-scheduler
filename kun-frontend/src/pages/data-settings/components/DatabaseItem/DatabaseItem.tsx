import React, { memo, useState, useCallback } from 'react';
import { Modal, message } from 'antd';
import { ReloadOutlined } from '@ant-design/icons';
import { DataBase } from '@/rematch/models/dataSettings';

import useI18n from '@/hooks/useI18n';
import useRedux from '@/hooks/useRedux';
import { watermarkFormatter } from '@/utils';

import styles from './DatabaseItem.less';

interface Props {
  database: DataBase;
  onClickUpdate: (database: DataBase) => void;
  onClickDelete: (id: string) => void;
}

export default memo(function DatabaseItem({
  database,
  onClickDelete,
  onClickUpdate,
}: Props) {
  const t = useI18n();

  const { selector, dispatch } = useRedux(state => ({
    databaseTypeFieldMapList: state.dataSettings.databaseTypeFieldMapList,
  }));

  const [pullLoading, setPullLoading] = useState(false);

  const handleClickPull = useCallback(() => {
    setPullLoading(true);
    dispatch.dataSettings.pullDatasetsFromDatabase(database.id).then(resp => {
      if (resp) {
        message.success(t('dataSettings.databaseItem.pullingIntoProgress'));
      }
      setPullLoading(false);
    });
  }, [database.id, dispatch.dataSettings, t]);

  const handleClickDelete = useCallback(() => {
    Modal.confirm({
      cancelText: t('common.button.cancel'),
      okText: t('common.button.confirm'),
      content: t('dataSettings.databaseItem.confirmDelete'),
      onOk: () => {
        onClickDelete(database.id);
      },
    });
  }, [database.id, onClickDelete, t]);

  const getDbType = (typeId: string | null) =>
    typeId
      ? selector.databaseTypeFieldMapList.find(i => i.id === typeId)?.type
      : '';

  return (
    <div className={styles.database}>
      <div className={styles.infoArea}>
        <div className={styles.nameRow}>
          <span className={styles.name}>{database.name}</span>
          <span className={styles.dbType}>{getDbType(database.typeId)}</span>
        </div>
        <div className={styles.updateUser}>
          {`${t('dataSettings.updateUser')}: ${database.update_user}`}
        </div>
      </div>

      <div className={styles.operateArea}>
        <span className={styles.updateTime}>
          {pullLoading
            ? t('dataSettings.databaseItem.pulling')
            : t('dataSettings.databaseItem.updatedOn', {
                time: watermarkFormatter(database.update_time),
              })}
        </span>

        <ReloadOutlined
          style={{ marginRight: 16, cursor: 'pointer' }}
          onClick={handleClickPull}
          spin={pullLoading}
        />

        <span className={styles.deleteButton} onClick={handleClickDelete}>
          {t('common.button.delete')}
        </span>

        <span
          className={styles.changeButton}
          onClick={() => onClickUpdate(database)}
        >
          {t('common.button.change')}
        </span>
      </div>
    </div>
  );
});
