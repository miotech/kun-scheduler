import React, { memo, useMemo, useState, useEffect, useCallback } from 'react';

import { Modal, Input, Select, Button, InputNumber } from 'antd';
import _ from 'lodash';

import { watermarkFormatter } from '@/utils';

import useI18n from '@/hooks/useI18n';
import useRedux from '@/hooks/useRedux';

import {
  UpdateDatabaseInfo,
  DatabaseInfo,
  DataBase,
  DatabaseTypeItemFieldItem,
} from '@/rematch/models/dataSettings';
import styles from './AddUpdateDatabaseModal.less';

interface Props {
  visible: boolean;
  database?: DataBase | null;
  onClose: () => void;
  onConfirm: (newDatabase: UpdateDatabaseInfo | DatabaseInfo) => void;
}

const initDatabaseInfo: DatabaseInfo = {
  typeId: null,
  name: '',
  information: {},
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
    databaseTypeFieldMapList: state.dataSettings.databaseTypeFieldMapList,
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

  const [
    currentDatabaseTypeFieldMap,
    setCurrentDatabaseTypeFieldMap,
  ] = useState<DatabaseTypeItemFieldItem[]>([]);

  useEffect(() => {
    const currentFiledMap = selector.databaseTypeFieldMapList.find(
      mapItem => mapItem.id === newDatabase.typeId,
    );
    if (currentFiledMap) {
      setCurrentDatabaseTypeFieldMap(currentFiledMap.fields);
    } else {
      setCurrentDatabaseTypeFieldMap([]);
    }
  }, [newDatabase.typeId, selector.databaseTypeFieldMapList]);

  const allDatabaseTypes = useMemo(
    () => selector.databaseTypeFieldMapList || [],
    [selector.databaseTypeFieldMapList],
  );

  const handleUpdateNewDatabase = useCallback((v, k) => {
    setNewDatabase(b => ({
      ...b,
      [k]: v,
    }));
  }, []);

  const handleChangeFuncMaps = useMemo(
    () => ({
      type: (v: string) => handleUpdateNewDatabase(v, 'typeId'),
      tags: (v: string[]) => handleUpdateNewDatabase(v, 'tags'),
      name: (e: React.ChangeEvent<HTMLInputElement>) =>
        handleUpdateNewDatabase(e.target.value, 'name'),
    }),
    [handleUpdateNewDatabase],
  );

  const handleUpdateNewDatabaseInformation = useCallback((v, k) => {
    setNewDatabase(b => ({
      ...b,
      information: {
        ...b.information,
        [k]: v,
      },
    }));
  }, []);

  const handleChangeInformationFuncMaps = useMemo(() => {
    const resultMap: any = {};
    currentDatabaseTypeFieldMap.forEach(field => {
      if (field.format === 'NUMBER_INPUT') {
        resultMap[field.key] = (v: number) =>
          handleUpdateNewDatabaseInformation(v, field.key);
      } else {
        resultMap[field.key] = (e: any) =>
          handleUpdateNewDatabaseInformation(e.target.value, field.key);
      }
    });
    return resultMap;
  }, [currentDatabaseTypeFieldMap, handleUpdateNewDatabaseInformation]);

  const handleClickConfirm = useCallback(() => {
    const { information, typeId, ...otherParams } = newDatabase;
    const currentFiledMap = selector.databaseTypeFieldMapList.find(
      mapItem => mapItem.id === typeId,
    );
    const currrentFieldList = currentFiledMap?.fields;
    const newInformation: any = {};
    currrentFieldList?.forEach(field => {
      newInformation[field.key] = information[field.key] ?? null;
    });
    const resultDatabase = {
      information: newInformation,
      typeId,
      ...otherParams,
    };
    onConfirm(resultDatabase);
  }, [newDatabase, onConfirm, selector.databaseTypeFieldMapList]);

  const disableConfirm = useMemo(() => {
    let disable = false;
    currentDatabaseTypeFieldMap.forEach(field => {
      if (!newDatabase.information[field.key] && field.require) {
        disable = true;
      }
    });
    if (!newDatabase.name) {
      disable = true;
    }
    if (!newDatabase.typeId) {
      disable = true;
    }
    return disable;
  }, [
    currentDatabaseTypeFieldMap,
    newDatabase.information,
    newDatabase.name,
    newDatabase.typeId,
  ]);

  const inputComp = (field: DatabaseTypeItemFieldItem) => {
    let comp: any;
    switch (field.format) {
      case 'INPUT':
        comp = (
          <div key={field.key} className={styles.inputItem}>
            <div className={styles.inputTitle}>
              {t(`dataSettings.field.${field.key}`)}
              {field.require && <span className={styles.required}>*</span>}
            </div>
            <div className={styles.inputComp}>
              <Input
                value={newDatabase.information[field.key]}
                onChange={handleChangeInformationFuncMaps[field.key]}
              />
            </div>
          </div>
        );
        break;
      case 'PASSWORD':
        comp = (
          <div key={field.key} className={styles.inputItem}>
            <div className={styles.inputTitle}>
              {t(`dataSettings.field.${field.key}`)}
              {field.require && <span className={styles.required}>*</span>}
            </div>
            <div className={styles.inputComp}>
              <Input
                type="password"
                value={newDatabase.information[field.key]}
                onChange={handleChangeInformationFuncMaps[field.key]}
              />
            </div>
          </div>
        );
        break;
      case 'NUMBER_INPUT':
        comp = (
          <div key={field.key} className={styles.inputItem}>
            <div className={styles.inputTitle}>
              {t(`dataSettings.field.${field.key}`)}
              {field.require && <span className={styles.required}>*</span>}
            </div>
            <div className={styles.inputComp}>
              <InputNumber
                style={{ width: '100%' }}
                value={
                  newDatabase.information[field.key]
                    ? Number(newDatabase.information[field.key])
                    : undefined
                }
                onChange={handleChangeInformationFuncMaps[field.key]}
              />
            </div>
          </div>
        );
        break;

      default:
        break;
    }
    return comp;
  };

  const informationCompList = _.orderBy(
    currentDatabaseTypeFieldMap,
    'order',
  ).map(field => inputComp(field));

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
            <span className={styles.required}>*</span>
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
            {t('dataSettings.addUpdate.dbType')}
            <span className={styles.required}>*</span>
          </div>
          <div className={styles.inputComp}>
            <Select
              style={{ width: '100%' }}
              value={newDatabase.typeId || undefined}
              onChange={handleChangeFuncMaps.type}
            >
              {allDatabaseTypes.map(type => (
                <Option key={type.id} value={type.id}>
                  {type.type}
                </Option>
              ))}
            </Select>
          </div>
        </div>

        {informationCompList}

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
