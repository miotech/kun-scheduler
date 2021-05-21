import React, { memo, useState, useCallback, useMemo } from 'react';
import { Modal, message, Skeleton } from 'antd';
import { ReloadOutlined } from '@ant-design/icons';
import { dayjs } from '@/utils/datetime-utils';
import { DataSource } from '@/rematch/models/dataSettings';

import useI18n from '@/hooks/useI18n';
import useRedux from '@/hooks/useRedux';
import { watermarkFormatter } from '@/utils/glossaryUtiles';

import { StatusText } from '@/components/StatusText';
import styles from './DatabaseItem.less';

interface Props {
  database: DataSource;
  onClickUpdate: (database: DataSource) => void;
  onClickDelete: (id: string) => void;
}

export default memo(function DatabaseItem({ database, onClickDelete, onClickUpdate }: Props) {
  const t = useI18n();

  const { selector, dispatch } = useRedux(state => ({
    databaseTypeFieldMapList: state.dataSettings.databaseTypeFieldMapList,
    latestPullProcessesMap: state.dataSettings.pullProcesses,
    latestPullProcessesIsLoading: state.dataSettings.pullProcessesIsLoading,
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
    typeId ? selector.databaseTypeFieldMapList.find(i => i.id === typeId)?.type : '';

  const pullProcessInfo = useMemo(() => {
    if (selector.latestPullProcessesIsLoading) {
      return (
        <div>
          <Skeleton.Input size="small" active style={{ width: '200px', height: '16px' }} />
        </div>
      );
    }
    return selector.latestPullProcessesMap[database.id] != null ? (
      <div>
        <span>
          {t('dataSettings.databaseItem.pullProcessCreatedTimeAndStatus', {
            time: dayjs(selector.latestPullProcessesMap[database.id].createdAt).fromNow(),
          })}
        </span>
        <span>&nbsp;</span>
        <span>
          {selector.latestPullProcessesMap[database.id]?.latestMCETaskRun != null ? (
            <StatusText status={selector.latestPullProcessesMap[database.id]!.latestMCETaskRun!.status} />
          ) : (
            ''
          )}
        </span>
      </div>
    ) : (
      <></>
    );
  }, [database.id, selector.latestPullProcessesIsLoading, selector.latestPullProcessesMap, t]);

  return (
    <div className={styles.database}>
      <div className={styles.infoArea}>
        <div className={styles.nameRow}>
          <span className={styles.name}>{database.name}</span>
          <span className={styles.dbType}>{getDbType(database.typeId)}</span>
        </div>
        <div className={styles.updateUser}>{`${t('dataSettings.updateUser')}: ${database.update_user}`}</div>
      </div>

      <div className={styles.operateArea}>
        <div className={styles.updateTimeWrapper}>
          <span className={styles.updateTime}>
            {pullLoading
              ? t('dataSettings.databaseItem.pulling')
              : t('dataSettings.databaseItem.updatedOn', {
                  time: watermarkFormatter(database.update_time),
                })}
            {pullProcessInfo}
          </span>
        </div>

        <ReloadOutlined style={{ marginRight: 16, cursor: 'pointer' }} onClick={handleClickPull} spin={pullLoading} />

        <span className={styles.deleteButton} onClick={handleClickDelete}>
          {t('common.button.delete')}
        </span>

        <span className={styles.changeButton} onClick={() => onClickUpdate(database)}>
          {t('common.button.change')}
        </span>
      </div>
    </div>
  );
});
