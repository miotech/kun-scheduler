import React, { memo, useMemo, useState, useEffect, useCallback } from 'react';

import { Modal, Input, Select, Button } from 'antd';

import { watermarkFormatter } from '@/utils';

import useI18n from '@/hooks/useI18n';
import useRedux from '@/hooks/useRedux';
import { DbType } from '@/rematch/models';

import {
  UpdateDatabaseInfo,
  DatabaseInfo,
  DataBase,
} from '@/rematch/models/dataSettings';
import styles from './AddUpdateDatabaseModal.less';

interface Props {
  visible: boolean;
  database?: DataBase | null;
  onClose: () => void;
  onConfirm: (newDatabase: UpdateDatabaseInfo | DatabaseInfo) => void;
}

const initDatabaseInfo: DatabaseInfo = {
  type: null,
  name: '',
  ip: '',
  username: '',
  password: '',
  tags: [],
};

const { Option } = Select;

export default memo(function AddUpdateDatabaseModal({
  visible,
  database,
  onClose,
  onConfirm,
}: Props) {
  const t = useI18n();

  const { selector, dispatch } = useRedux(state => ({
    allTagList: state.dataDiscovery.allTagList,
  }));

  const modalTitle = useMemo(
    () => (database ? database.name : t('dataSettings.addDatabase')),
    [database, t],
  );

  const [newDatabase, setNewDatabase] = useState<
    UpdateDatabaseInfo | DatabaseInfo
  >(() => initDatabaseInfo);

  useEffect(() => {
    if (visible) {
      dispatch.dataDiscovery.fetchAllTagList();
      if (database) {
        setNewDatabase(database);
      } else {
        setNewDatabase(initDatabaseInfo);
      }
    }
  }, [database, dispatch.dataDiscovery, visible]);

  const handleUpdateNewDatabase = useCallback((v, k) => {
    setNewDatabase(b => ({
      ...b,
      [k]: v,
    }));
  }, []);

  const handleChangeFuncMaps = useMemo(
    () => ({
      name: (e: React.ChangeEvent<HTMLInputElement>) =>
        handleUpdateNewDatabase(e.target.value, 'name'),
      ip: (e: React.ChangeEvent<HTMLInputElement>) =>
        handleUpdateNewDatabase(e.target.value, 'ip'),
      username: (e: React.ChangeEvent<HTMLInputElement>) =>
        handleUpdateNewDatabase(e.target.value, 'username'),
      password: (e: React.ChangeEvent<HTMLInputElement>) =>
        handleUpdateNewDatabase(e.target.value, 'password'),
      type: (v: DbType) => handleUpdateNewDatabase(v, 'type'),
      tags: (v: string[]) => handleUpdateNewDatabase(v, 'tags'),
    }),
    [handleUpdateNewDatabase],
  );

  const handleClickConfirm = useCallback(() => {
    onConfirm(newDatabase);
  }, [newDatabase, onConfirm]);

  const disableConfirm = useMemo(
    () =>
      !newDatabase.name ||
      !newDatabase.ip ||
      !newDatabase.username ||
      !newDatabase.password ||
      !newDatabase.type,
    [
      newDatabase.ip,
      newDatabase.name,
      newDatabase.password,
      newDatabase.type,
      newDatabase.username,
    ],
  );

  return (
    <Modal
      width={747}
      bodyStyle={{ padding: 0 }}
      visible={visible}
      closable={false}
      footer={null}
      title={<div className={styles.modalTitle}>{modalTitle}</div>}
    >
      <div className={styles.modalBody}>
        <div className={styles.inputItem}>
          <div className={styles.inputTitle}>
            {t('dataSettings.addUpdate.name')}
          </div>
          <div className={styles.inputComp}>
            <Input
              value={newDatabase.name}
              onChange={handleChangeFuncMaps.name}
            />
          </div>
        </div>

        <div className={styles.inputItem}>
          <div className={styles.inputTitle}>
            {t('dataSettings.addUpdate.ipAddr')}
          </div>
          <div className={styles.inputComp}>
            <Input value={newDatabase.ip} onChange={handleChangeFuncMaps.ip} />
          </div>
        </div>

        <div className={styles.inputItem}>
          <div className={styles.inputTitle}>
            {t('dataSettings.addUpdate.dbType')}
          </div>
          <div className={styles.inputComp}>
            <Select
              style={{ width: '100%' }}
              value={newDatabase.type || undefined}
              onChange={handleChangeFuncMaps.type}
            >
              {Object.values(DbType).map(type => (
                <Option key={type} value={type}>
                  {type}
                </Option>
              ))}
            </Select>
          </div>
        </div>

        <div className={styles.inputItem}>
          <div className={styles.inputTitle}>
            {t('dataSettings.addUpdate.username')}
          </div>
          <div className={styles.inputComp}>
            <Input
              value={newDatabase.username}
              onChange={handleChangeFuncMaps.username}
            />
          </div>
        </div>

        <div className={styles.inputItem}>
          <div className={styles.inputTitle}>
            {t('dataSettings.addUpdate.password')}
          </div>
          <div className={styles.inputComp}>
            <Input
              value={newDatabase.password}
              onChange={handleChangeFuncMaps.password}
              type="password"
            />
          </div>
        </div>

        {database && (
          <>
            <div className={styles.inputItem}>
              <div className={styles.inputTitle}>
                {t('dataSettings.addUpdate.createUser')}
              </div>
              <div className={styles.inputComp}>
                <Input disabled value={database.create_user} />
              </div>
            </div>

            <div className={styles.inputItem}>
              <div className={styles.inputTitle}>
                {t('dataSettings.addUpdate.createTime')}
              </div>
              <div className={styles.inputComp}>
                <Input
                  disabled
                  value={watermarkFormatter(database.create_time)}
                />
              </div>
            </div>

            <div className={styles.inputItem}>
              <div className={styles.inputTitle}>
                {t('dataSettings.addUpdate.updateUser')}
              </div>
              <div className={styles.inputComp}>
                <Input disabled value={database.update_user} />
              </div>
            </div>

            <div className={styles.inputItem}>
              <div className={styles.inputTitle}>
                {t('dataSettings.addUpdate.updateTime')}
              </div>
              <div className={styles.inputComp}>
                <Input
                  disabled
                  value={watermarkFormatter(database.update_time)}
                />
              </div>
            </div>
          </>
        )}

        <div className={styles.inputItem}>
          <div className={styles.inputTitle}>
            {t('dataSettings.addUpdate.tags')}
          </div>
          <div className={styles.inputComp}>
            <Select
              style={{ width: '100%' }}
              mode="tags"
              value={newDatabase.tags || []}
              onChange={handleChangeFuncMaps.tags}
            >
              {selector.allTagList.map(tag => (
                <Option key={tag} value={tag}>
                  {tag}
                </Option>
              ))}
            </Select>
          </div>
        </div>
      </div>
      <div className={styles.footer}>
        <Button size="large" style={{ marginRight: 24 }} onClick={onClose}>
          {t('common.button.cancel')}
        </Button>
        <Button
          disabled={disableConfirm}
          size="large"
          type="primary"
          onClick={handleClickConfirm}
        >
          {database ? t('common.button.update') : t('common.button.add')}
        </Button>
      </div>
    </Modal>
  );
});
