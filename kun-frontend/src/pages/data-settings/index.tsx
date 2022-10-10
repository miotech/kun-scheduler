import React, { useCallback, useEffect } from 'react';
import { Input, Button, Spin, Pagination } from 'antd';
import useI18n from '@/hooks/useI18n';
import useRedux from '@/hooks/useRedux';
import useDebounce from '@/hooks/useDebounce';
import { DataSourceInfo } from '@/definitions/DataSource.type';
import { useHistory } from 'umi';
import Card from '@/components/Card/Card';
import DatabaseItem from './components/DatabaseItem/DatabaseItem';

import styles from './index.less';

const { Search } = Input;

export default function DataSettings() {
  const t = useI18n();
  const { selector, dispatch } = useRedux(state => state.dataSettings);
  const history = useHistory();

  const doRefresh = useCallback(() => {
    dispatch.dataSettings.searchDataBases();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    doRefresh();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const { searchContent, pagination, searchLoading } = selector;

  const handleSearch = useCallback(() => {
    dispatch.dataSettings.searchDataBases();
  }, [dispatch]);

  const debounceSearchContent = useDebounce(searchContent, 300);

  useEffect(() => {
    handleSearch();
  }, [dispatch, pagination.pageNumber, pagination.pageSize, handleSearch, debounceSearchContent]);

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

  const handleClickAddDatabase = useCallback(() => {
    history.push('/settings/data-source/add');
  }, [history]);

  const handleClickUpdateDatabase = useCallback(
    (database: DataSourceInfo) => {
      history.push(`/settings/data-source/detail?id=${database.id}`);
    },
    [history],
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
          />
          <div className={styles.ButtonsGroupRight}>
            <Button type="default" size="large" onClick={doRefresh}>
              {t('common.refresh')}
            </Button>
            <Button size="large" type="primary" onClick={handleClickAddDatabase}>
              {t('dataSettings.addDatasource')}
            </Button>
          </div>
        </div>

        <div className={styles.databasesArea}>
          <Spin spinning={searchLoading}>
            <div className={styles.databasesCount}>
              {t('dataSettings.datasourceCount', {
                count: selector.dataSourceList?.length ?? 0,
              })}
            </div>

            {selector.dataSourceList?.map(database => (
              <DatabaseItem
                key={database.id}
                database={database}
                onClickDelete={handleClickDeleteDatabase}
                onClickUpdate={handleClickUpdateDatabase}
              />
            ))}
          </Spin>
        </div>

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
      </Card>
    </div>
  );
}
