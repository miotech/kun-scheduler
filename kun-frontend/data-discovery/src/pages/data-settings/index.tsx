import React, { useMemo, useCallback, useState, useEffect } from 'react';
import { Input, Button, Spin, message, Pagination } from 'antd';
import _ from 'lodash';
import { RematchDispatch } from '@rematch/core';

import { RootModel } from '@/rematch/models';
import {
  DatabaseInfo,
  UpdateDatabaseInfo,
  DataBase,
} from '@/rematch/models/dataSettings';

import useI18n from '@/hooks/useI18n';
import useRedux from '@/hooks/useRedux';

import Card from '@/components/Card/Card';

import AddUpdateDatabaseModal from './components/AddUpdateDatabaseModal/AddUpdateDatabaseModal';

import styles from './index.less';
import DatabaseItem from './components/DatabaseItem/DatabaseItem';

const { Search } = Input;

export default function DataSettings() {
  const t = useI18n();
  const { selector, dispatch } = useRedux(state => state.dataSettings);

  const { searchContent, pagination, currentDatabase } = selector;

  const [searchFuncLoading, setSearchFuncLoading] = useState(false);

  useEffect(() => {
    dispatch.dataSettings.updateState({
      key: 'pagination',
      value: {
        pageSize: pagination.pageSize,
        pageNumber: 1,
      },
    });
  }, [dispatch.dataSettings, pagination.pageSize, searchContent]);

  const searchFunc = useMemo(
    () =>
      _.debounce(async (theDispatch: RematchDispatch<RootModel>) => {
        setSearchFuncLoading(true);
        theDispatch.dataSettings.searchDataBases().then(() => {
          setSearchFuncLoading(false);
        });
      }, 500),
    [],
  );

  const handleSearch = useCallback(() => {
    searchFunc(dispatch);
  }, [dispatch, searchFunc]);

  useEffect(() => {
    searchFunc(dispatch);
  }, [dispatch, searchFunc, pagination.pageNumber, pagination.pageSize]);

  const [addDatabaseModalVisible, setAddDatabaseModalVisible] = useState(false);

  const handleCloseAddDatabaseModal = useCallback(() => {
    setAddDatabaseModalVisible(false);
  }, []);

  const handleConfirmAddDatabaseModal = useCallback(
    (newDatabase: DatabaseInfo) => {
      dispatch.dataSettings.addDatabase(newDatabase).then(resp => {
        if (resp) {
          message.success(t('common.operateSuccess'));
          handleSearch();
          setAddDatabaseModalVisible(false);
        }
      });
    },
    [dispatch.dataSettings, handleSearch, t],
  );

  const [updateDatabaseModalVisible, setUpdateDatabaseModalVisible] = useState(
    false,
  );

  const handleCloseUpdateDatabaseModal = useCallback(() => {
    setUpdateDatabaseModalVisible(false);
  }, []);

  const updateDatabase = useCallback(
    (newDatabase: DataBase) => {
      const { dataBaseList } = selector;
      const newDataBaseList = dataBaseList.map(i => {
        if (i.id === newDatabase.id) {
          return newDatabase;
        }
        return i;
      });
      dispatch.dataSettings.updateState({
        key: 'dataBaseList',
        value: newDataBaseList,
      });
    },
    [dispatch.dataSettings, selector],
  );

  const handleConfirmUpdateDatabaseModal = useCallback(
    (newDatabase: UpdateDatabaseInfo) => {
      const { id, type, name, ip, username, password, tags } = newDatabase;
      const params = { id, type, name, ip, username, password, tags };
      dispatch.dataSettings.updateDatabase(params).then(resp => {
        if (resp) {
          message.success(t('common.operateSuccess'));
          updateDatabase(resp);
          setUpdateDatabaseModalVisible(false);
        }
      });
    },
    [dispatch.dataSettings, t, updateDatabase],
  );

  const handleClickDeleteDatabase = useCallback(
    (id: string) => {
      dispatch.dataSettings.deleteDatabase(id).then(resp => {
        if (resp) {
          handleSearch();
        }
      });
    },
    [dispatch.dataSettings, handleSearch],
  );

  const handleClickUpdateDatabase = useCallback(
    (database: DataBase) => {
      dispatch.dataSettings.updateState({
        key: 'currentDatabase',
        value: database,
      });
      setUpdateDatabaseModalVisible(true);
    },
    [dispatch.dataSettings],
  );

  const handleChangePagination = useCallback(
    (pageNumber: number, pageSize?: number) => {
      dispatch.dataSettings.updateState({
        key: 'pagination',
        value: {
          ...pagination,
          pageNumber,
          pageSize: pageSize || 25,
        },
      });
    },
    [dispatch.dataSettings, pagination],
  );

  return (
    <div className={styles.page}>
      <Card>
        <div className={styles.searchBar}>
          <Search
            size="large"
            className={styles.searchInput}
            value={searchContent}
            onChange={e =>
              dispatch.dataSettings.updateState({
                key: 'searchContent',
                value: e.target.value,
              })
            }
            onSearch={handleSearch}
          />
          <Button
            size="large"
            className={styles.addDatabaseButton}
            onClick={() => setAddDatabaseModalVisible(true)}
          >
            {t('dataSettings.addDatabase')}
          </Button>
        </div>

        <div className={styles.databasesArea}>
          <Spin spinning={searchFuncLoading}>
            <div className={styles.databasesCount}>
              {t('dataSettings.databasesCount', {
                count: selector.dataBaseList.length,
              })}
            </div>

            {selector.dataBaseList.map(database => (
              <DatabaseItem
                key={database.id}
                database={database}
                onClickDelete={handleClickDeleteDatabase}
                onClickUpdate={handleClickUpdateDatabase}
              />
            ))}
          </Spin>
        </div>

        {(pagination.totalCount || 0) > pagination.pageSize && (
          <div className={styles.pagination}>
            <Pagination
              size="small"
              total={pagination.totalCount}
              showSizeChanger
              showQuickJumper
              onChange={handleChangePagination}
              pageSize={pagination.pageSize}
              pageSizeOptions={['25', '50', '100', '200']}
            />
          </div>
        )}
      </Card>

      <AddUpdateDatabaseModal
        visible={addDatabaseModalVisible}
        onClose={handleCloseAddDatabaseModal}
        onConfirm={handleConfirmAddDatabaseModal}
      />

      <AddUpdateDatabaseModal
        database={currentDatabase}
        visible={updateDatabaseModalVisible}
        onClose={handleCloseUpdateDatabaseModal}
        onConfirm={
          handleConfirmUpdateDatabaseModal as (
            newDatabase: UpdateDatabaseInfo | DatabaseInfo,
          ) => void
        }
      />
    </div>
  );
}
