import React, { memo, useMemo, useState, useEffect, useCallback } from 'react';

import { Modal, Input, Select, Button, Radio } from 'antd';
import _ from 'lodash';

import { watermarkFormatter } from '@/utils/glossaryUtiles';

import useI18n from '@/hooks/useI18n';
import useRedux from '@/hooks/useRedux';

import { UpdateDatasourceInfo, DatasourceInfo, DataSource } from '@/rematch/models/dataSettings';
import {
  dataSourceList,
  DataSourceType,
  HiveTempType,
  getConnectionContentMap,
  otherConnections,
  hiveConnections,
  ConnectionItem,
  getCurrentFields,
  isSame,
} from './fieldMap';
import styles from './AddUpdateDatabaseModal.less';

interface Props {
  visible: boolean;
  database?: DataSource | null;
  onClose: () => void;
  onConfirm: (newDatabase: UpdateDatasourceInfo | DatasourceInfo) => void;
}

const initDatabaseInfo: DatasourceInfo = {
  datasourceType: null,
  name: '',
  information: {},
  tags: [],
};

const { Option } = Select;

export default memo(function AddUpdateDatabaseModal({ visible, database, onClose, onConfirm }: Props) {
  const t = useI18n();

  const { selector, dispatch } = useRedux(state => ({
    allTagList: state.dataDiscovery.allTagList,
    databaseTypeFieldMapList: state.dataSettings.databaseTypeFieldMapList,
  }));

  const modalTitle = useMemo(() => (database ? database.name : t('dataSettings.addDatasource')), [database, t]);

  const [newDatabase, setNewDatabase] = useState<UpdateDatasourceInfo | DatasourceInfo>(() => initDatabaseInfo);

  useEffect(() => {
    if (visible) {
      dispatch.dataDiscovery.fetchAllTagList();
      if (database) {
        const tempDatabase = { ...database };
        const newInformation = { ...tempDatabase.information };
        const { userConnection } = newInformation;
        _.forEach(tempDatabase.information, (v, k) => {
          if (v && k !== 'userConnection') {
            if (isSame(userConnection, v)) {
              newInformation[k] = {
                connectionType: HiveTempType.SAME,
              };
            }
          }
        });

        tempDatabase.information = newInformation;

        setNewDatabase(tempDatabase);
      } else {
        setNewDatabase(initDatabaseInfo);
      }
    }
  }, [database, dispatch.dataDiscovery, visible]);

  const handleUpdateNewDatabase = useCallback((v, k) => {
    let information = {};

    if (k === 'datasourceType') {
      if (v === DataSourceType.HIVE) {
        const informationContent = getConnectionContentMap(HiveTempType.HIVE_SERVER);
        information = {
          userConnection: {
            connectionType: HiveTempType.HIVE_SERVER,
            ...informationContent,
          },
          storageConnection: {
            connectionType: HiveTempType.SAME,
          },
          dataConnection: {
            connectionType: HiveTempType.SAME,
          },
          metadataConnection: {
            connectionType: HiveTempType.SAME,
          },
        };
      } else {
        const informationContent = getConnectionContentMap(HiveTempType.HIVE_SERVER);
        information = {
          userConnection: {
            connectionType: v,
            ...informationContent,
          },
        };
      }
      setNewDatabase(b => ({
        ...b,
        [k]: v,
        information,
      }));
    } else {
      setNewDatabase(b => ({
        ...b,
        [k]: v,
      }));
    }
  }, []);

  const handleChangeFuncMaps = useMemo(
    () => ({
      datasourceType: (v: string) => handleUpdateNewDatabase(v, 'datasourceType'),
      tags: (v: string[]) => handleUpdateNewDatabase(v, 'tags'),
      name: (e: React.ChangeEvent<HTMLInputElement>) => handleUpdateNewDatabase(e.target.value, 'name'),
    }),
    [handleUpdateNewDatabase],
  );

  const hanldeUpdateConnectionTempType = useCallback((t1: HiveTempType, connection: string) => {
    setNewDatabase(b => ({
      ...b,
      information: {
        ...b.information,
        [connection]: {
          connectionType: t1,
          ...getConnectionContentMap(t1),
        },
      },
    }));
  }, []);

  const handleUpdateNewDatabaseInformation = useCallback((v, k, connection: string) => {
    setNewDatabase(b => ({
      ...b,
      information: {
        ...b.information,
        [connection]: {
          ...(b.information as any)[connection],
          [k]: v,
        },
      },
    }));
  }, []);

  const handleChangeInformationFunc = useCallback(
    (e, field, connection) => {
      handleUpdateNewDatabaseInformation(e.target.value, field, connection);
    },
    [handleUpdateNewDatabaseInformation],
  );
  const handleClickConfirm = useCallback(() => {
    const { information, datasourceType, ...otherParams } = newDatabase;

    const { userConnection } = information;
    const resultInformation = { ...information };
    _.forEach(information, (v, k) => {
      if (v && k !== 'userConnection') {
        const { connectionType } = v;
        if (connectionType === HiveTempType.SAME) {
          resultInformation[k] = userConnection;
        }
      }
    });

    const resultDatabase = { datasourceType, ...otherParams, information: resultInformation };

    onConfirm(resultDatabase);
  }, [newDatabase, onConfirm]);

  const disableConfirm = useMemo(() => {
    return false;
  }, []);

  const inputComp = useCallback(
    (field: { field: string; type: string; require?: boolean }, connection: string) => {
      let comp: any;
      switch (field.type) {
        case 'string':
          comp = (
            <div key={field.field} className={styles.inputItem}>
              <div className={styles.inputTitle}>
                {t(`dataSettings.field.${field.field}`)}
                {field.require && <span className={styles.required}>*</span>}
              </div>
              <div className={styles.inputComp}>
                <Input
                  value={newDatabase?.information?.[connection]?.[field.field]}
                  onChange={e => handleChangeInformationFunc(e, field.field, connection)}
                />
              </div>
            </div>
          );
          break;
        case 'password':
          comp = (
            <div key={field.field} className={styles.inputItem}>
              <div className={styles.inputTitle}>
                {t(`dataSettings.field.${field.field}`)}
                {field.require && <span className={styles.required}>*</span>}
              </div>
              <div className={styles.inputComp}>
                <Input
                  type="password"
                  value={newDatabase?.information?.[connection]?.[field.field]}
                  onChange={e => handleChangeInformationFunc(e, field.field, connection)}
                />
              </div>
            </div>
          );
          break;

        default:
          break;
      }
      return comp;
    },
    [handleChangeInformationFunc, newDatabase.information, t],
  );

  const currentConnections: ConnectionItem[] | null = useMemo(() => {
    if (!newDatabase.datasourceType) {
      return null;
    }
    if (newDatabase.datasourceType !== DataSourceType.HIVE) {
      return otherConnections;
    }
    return hiveConnections;
  }, [newDatabase.datasourceType]);

  const getConnectionUI = useCallback(
    (c: ConnectionItem) => {
      if (!c) {
        return null;
      }
      const connectionTypeValue = newDatabase.information?.[c.connection]?.connectionType;
      const radioOptions = c.templateList;
      const fields = getCurrentFields(connectionTypeValue as HiveTempType);
      return (
        <div className={styles.connectionSection} key={c.connection}>
          <div className={styles.connectionTitle}>{t(`dataSettings.addUpdate.connectionTitle.${c.connection}`)}</div>
          {radioOptions.length > 1 && (
            <div className={styles.inputItem}>
              <div className={styles.inputTitle}>{t('dataSettings.field.accessMethod')}</div>
              <div className={styles.inputComp}>
                <Radio.Group
                  onChange={(v: any) => hanldeUpdateConnectionTempType(v.target.value, c.connection)}
                  value={connectionTypeValue}
                >
                  {radioOptions.map(i => (
                    <Radio value={i} key={i}>
                      {t(`dataSettings.addUpdate.connectionTypeOption.${i}`)}
                    </Radio>
                  ))}
                </Radio.Group>
              </div>
            </div>
          )}
          {fields.map(field => (
            <div key={field.field}>{inputComp(field, c.connection)}</div>
          ))}
        </div>
      );
    },
    [hanldeUpdateConnectionTempType, inputComp, newDatabase, t],
  );

  const connectionUIList = useMemo(() => {
    if (!currentConnections) {
      return null;
    }
    return currentConnections.map(i => getConnectionUI(i));
  }, [currentConnections, getConnectionUI]);

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
            <Input value={newDatabase.name} onChange={handleChangeFuncMaps.name} />
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
              value={newDatabase.datasourceType || undefined}
              onChange={handleChangeFuncMaps.datasourceType}
            >
              {dataSourceList.map(type => (
                <Option key={type} value={type}>
                  {type}
                </Option>
              ))}
            </Select>
          </div>
        </div>

        {connectionUIList}

        {database && (
          <>
            <div className={styles.inputItem}>
              <div className={styles.inputTitle}>{t('dataSettings.addUpdate.createUser')}</div>
              <div className={styles.inputComp}>
                <Input disabled value={database.create_user} />
              </div>
            </div>

            <div className={styles.inputItem}>
              <div className={styles.inputTitle}>{t('dataSettings.addUpdate.createTime')}</div>
              <div className={styles.inputComp}>
                <Input disabled value={watermarkFormatter(database.create_time)} />
              </div>
            </div>

            <div className={styles.inputItem}>
              <div className={styles.inputTitle}>{t('dataSettings.addUpdate.updateUser')}</div>
              <div className={styles.inputComp}>
                <Input disabled value={database.update_user} />
              </div>
            </div>

            <div className={styles.inputItem}>
              <div className={styles.inputTitle}>{t('dataSettings.addUpdate.updateTime')}</div>
              <div className={styles.inputComp}>
                <Input disabled value={watermarkFormatter(database.update_time)} />
              </div>
            </div>
          </>
        )}

        <div className={styles.inputItem}>
          <div className={styles.inputTitle}>{t('dataSettings.addUpdate.tags')}</div>
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
        <Button disabled={disableConfirm} size="large" type="primary" onClick={handleClickConfirm}>
          {database ? t('common.button.update') : t('common.button.add')}
        </Button>
      </div>
    </Modal>
  );
});
