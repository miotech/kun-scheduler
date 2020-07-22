import React, { useCallback, useMemo, useEffect } from 'react';
import { Link } from 'umi';
import { Input, Select, Table, Tag, Spin } from 'antd';
import { ColumnProps } from 'antd/es/table';
import { PaginationProps } from 'antd/es/pagination';
import { useDispatch, useSelector, shallowEqual } from 'react-redux';
import { RootDispatch, RootState } from '@/rematch/store';
import { Mode, Dataset, Watermark } from '@/rematch/models/dataDiscovery';

import color from '@/styles/color';

import useI18n from '@/hooks/useI18n';
import useDebounce from '@/hooks/useDebounce';

import Card from '@/components/Card/Card';
import { watermarkFormatter } from '@/utils';
import TimeSelect from './components/TimeSelect/TimeSelect';

import styles from './index.less';

const { Search } = Input;
const { Option } = Select;

export default function DataDisvocery() {
  const t = useI18n();

  const dispatch = useDispatch<RootDispatch>();
  const {
    searchContent,
    watermarkMode,
    watermarkAbsoluteValue,
    watermarkQuickeValue,

    dbTypeList,
    ownerList,
    tagList,
    dsIdList,

    allOwnerList,
    allTagList,
    allDbList,

    datasetList,

    pagination,
    dataListFetchLoading,
    databaseTypes,
  } = useSelector(
    (state: RootState) => ({
      searchContent: state.dataDiscovery.searchContent,
      watermarkMode: state.dataDiscovery.watermarkMode,
      watermarkAbsoluteValue: state.dataDiscovery.watermarkAbsoluteValue,
      watermarkQuickeValue: state.dataDiscovery.watermarkQuickeValue,

      dbTypeList: state.dataDiscovery.dbTypeList,
      ownerList: state.dataDiscovery.ownerList,
      tagList: state.dataDiscovery.tagList,
      dsIdList: state.dataDiscovery.dsIdList,

      allOwnerList: state.dataDiscovery.allOwnerList,
      allTagList: state.dataDiscovery.allTagList,
      allDbList: state.dataDiscovery.allDbList,

      datasetList: state.dataDiscovery.datasetList,
      pagination: state.dataDiscovery.pagination,
      dataListFetchLoading: state.dataDiscovery.dataListFetchLoading,
      databaseTypes: state.dataSettings.databaseTypeFieldMapList,
    }),
    shallowEqual,
  );

  useEffect(() => {
    dispatch.dataDiscovery.fetchAllOwnerList();
    dispatch.dataDiscovery.fetchAllTagList();
    dispatch.dataSettings.fetchDatabaseTypeList();
    dispatch.dataDiscovery.fetchAllDb('');
  }, [dispatch.dataDiscovery, dispatch.dataSettings]);

  const debounceSearchContent = useDebounce(searchContent, 1000);

  useEffect(() => {
    dispatch.dataDiscovery.searchDatasets({
      searchContent: debounceSearchContent,
      ownerList,
      tagList,
      dbTypeList,
      dsIdList,
      watermarkMode,
      watermarkAbsoluteValue,
      watermarkQuickeValue,
      pagination: {
        pageSize: pagination.pageSize,
        pageNumber: pagination.pageNumber || 1,
      },
    });
  }, [
    dbTypeList,
    debounceSearchContent,
    dispatch.dataDiscovery,
    dsIdList,
    ownerList,
    pagination.pageNumber,
    pagination.pageSize,
    tagList,
    watermarkAbsoluteValue,
    watermarkMode,
    watermarkQuickeValue,
  ]);

  const handleChangeSearch = useCallback(
    e => {
      dispatch.dataDiscovery.updateFilter({
        key: 'searchContent',
        value: e.target.value,
      });
    },
    [dispatch],
  );

  const handleChangeWatermarkMode = useCallback(
    mode => {
      dispatch.dataDiscovery.updateFilter({
        key: 'watermarkMode',
        value: mode,
      });
    },
    [dispatch],
  );

  const timeSelectValue = useMemo(
    () =>
      watermarkMode === Mode.ABSOLUTE
        ? watermarkAbsoluteValue
        : watermarkQuickeValue,
    [watermarkMode, watermarkAbsoluteValue, watermarkQuickeValue],
  );

  const handleChangeWatermarkValue = useCallback(
    (v, mode) => {
      if (mode === Mode.ABSOLUTE) {
        dispatch.dataDiscovery.updateFilter({
          key: 'watermarkAbsoluteValue',
          value: v,
        });
      }
      if (mode === Mode.QUICK) {
        dispatch.dataDiscovery.updateFilter({
          key: 'watermarkQuickeValue',
          value: v,
        });
      }
    },
    [dispatch],
  );

  const columns: ColumnProps<Dataset>[] = useMemo(
    () =>
      [
        {
          title: t('dataDiscovery.datasetsTable.header.name'),
          dataIndex: 'name',
          key: 'name',
          width: 170,
          render: (name: string, record: Dataset) => (
            <Link
              className={styles.nameLink}
              to={`/data-discovery/dataset/${record.id}`}
            >
              {name}
            </Link>
          ),
        },
        {
          title: t('dataDiscovery.datasetsTable.header.database'),
          dataIndex: 'database',
          key: 'database',
          width: 80,
        },
        {
          title: t('dataDiscovery.datasetsTable.header.datasource'),
          dataIndex: 'datasource',
          key: 'datasource',
          width: 120,
        },
        {
          title: t('dataDiscovery.datasetsTable.header.dbtype'),
          dataIndex: 'type',
          key: 'type',
          width: 80,
        },
        {
          title: t('dataDiscovery.datasetsTable.header.watermark'),
          dataIndex: 'high_watermark',
          key: 'high_watermark',
          width: 150,
          render: (watermark: Watermark) => watermarkFormatter(watermark.time),
        },
        {
          title: t('dataDiscovery.datasetsTable.header.description'),
          dataIndex: 'description',
          key: 'description',
          width: 200,
        },
        {
          title: t('dataDiscovery.datasetsTable.header.owners'),
          dataIndex: 'owners',
          key: 'owners',
          width: 300,
          render: (owners: string[]) => (
            <>
              {(owners || []).map(owner => (
                <Tag
                  key={owner}
                  className="light-blue-tag"
                  color={color.lightBlue}
                >
                  {owner}
                </Tag>
              ))}
            </>
          ),
        },
        {
          title: t('dataDiscovery.datasetsTable.header.tags'),
          dataIndex: 'tags',
          key: 'tags',
          width: 300,
          render: (tags: string[]) => (
            <>
              {(tags || []).map(tag => (
                <Tag
                  key={tag}
                  className="light-blue-tag"
                  color={color.lightBlue}
                >
                  {tag}
                </Tag>
              ))}
            </>
          ),
        },
      ] as ColumnProps<Dataset>[],
    [t],
  );

  const scroll = useMemo(
    () => ({
      x: 2000,
    }),
    [],
  );

  const handleChangePage = useCallback(
    (pageNumber, pageSize) => {
      dispatch.dataDiscovery.updateState({
        key: 'pagination',
        value: {
          ...pagination,
          pageNumber,
          pageSize,
        },
      });
    },
    [dispatch.dataDiscovery, pagination],
  );

  const handleChangePageSize = useCallback(
    (_pageNumber, pageSize) => {
      dispatch.dataDiscovery.updateState({
        key: 'pagination',
        value: {
          ...pagination,
          pageNumber: 1,
          pageSize,
        },
      });
    },
    [dispatch.dataDiscovery, pagination],
  );

  const tablePagination: PaginationProps = useMemo(
    () => ({
      size: 'small',
      total: pagination.totalCount,
      current: pagination.pageNumber,
      pageSize: pagination.pageSize,
      pageSizeOptions: ['15', '25', '50', '100'],
      onChange: handleChangePage,
      onShowSizeChange: handleChangePageSize,
    }),
    [
      handleChangePage,
      handleChangePageSize,
      pagination.pageNumber,
      pagination.pageSize,
      pagination.totalCount,
    ],
  );

  const allDatabaseTypes = databaseTypes.map(item => ({
    name: item.type,
    id: item.id,
  }));

  return (
    <div className={styles.page}>
      <Card className={styles.content}>
        <div className={styles.searchArea}>
          <Search
            size="large"
            placeholder={t('dataDiscovery.searchContent')}
            onChange={handleChangeSearch}
            value={searchContent}
            style={{ width: '70%' }}
          />
        </div>
        <div className={styles.tableArea}>
          <div className={styles.filterRow}>
            <div className={styles.filterItem}>
              <div className={styles.filterItemTitle}>
                {t('dataDiscovery.waterMark')}
              </div>
              <div>
                <TimeSelect
                  mode={watermarkMode}
                  onModeChange={handleChangeWatermarkMode}
                  value={timeSelectValue}
                  onChange={handleChangeWatermarkValue}
                />
              </div>
            </div>

            <div className={styles.filterItem}>
              <div className={styles.filterItemTitle}>
                {t('dataDiscovery.datasource')}
              </div>
              <div className={styles.filterItemSelect}>
                <Select
                  value={dsIdList}
                  mode="multiple"
                  size="large"
                  onChange={v => {
                    dispatch.dataDiscovery.updateFilter({
                      key: 'dsIdList',
                      value: v,
                    });
                  }}
                  placeholder={t('dataDiscovery.pleaseSelect')}
                  allowClear
                >
                  {allDbList.map(option => (
                    <Option key={option.id} value={option.id}>
                      {option.name}
                    </Option>
                  ))}
                </Select>
              </div>
            </div>

            <div className={styles.filterItem}>
              <div className={styles.filterItemTitle}>
                {t('dataDiscovery.dbtype')}
              </div>
              <div className={styles.filterItemSelect}>
                <Select
                  value={dbTypeList}
                  mode="multiple"
                  size="large"
                  onChange={v => {
                    dispatch.dataDiscovery.updateFilter({
                      key: 'dbTypeList',
                      value: v,
                    });
                  }}
                  placeholder={t('dataDiscovery.pleaseSelect')}
                  allowClear
                >
                  {allDatabaseTypes.map(option => (
                    <Option key={option.id} value={option.id}>
                      {option.name}
                    </Option>
                  ))}
                </Select>
              </div>
            </div>

            <div className={styles.filterItem}>
              <div className={styles.filterItemTitle}>
                {t('dataDiscovery.owners')}
              </div>
              <div className={styles.filterItemSelect}>
                <Select
                  value={ownerList}
                  mode="multiple"
                  size="large"
                  onChange={v => {
                    dispatch.dataDiscovery.updateFilter({
                      key: 'ownerList',
                      value: v,
                    });
                  }}
                  placeholder={t('dataDiscovery.pleaseSelect')}
                  allowClear
                >
                  {allOwnerList.map(option => (
                    <Option key={option} value={option}>
                      {option}
                    </Option>
                  ))}
                </Select>
              </div>
            </div>

            <div className={styles.filterItem}>
              <div className={styles.filterItemTitle}>
                {t('dataDiscovery.tags')}
              </div>
              <div className={styles.filterItemSelect}>
                <Select
                  value={tagList}
                  mode="multiple"
                  size="large"
                  onChange={v => {
                    dispatch.dataDiscovery.updateFilter({
                      key: 'tagList',
                      value: v,
                    });
                  }}
                  placeholder={t('dataDiscovery.pleaseSelect')}
                  allowClear
                >
                  {allTagList.map(option => (
                    <Option key={option} value={option}>
                      {option}
                    </Option>
                  ))}
                </Select>
              </div>
            </div>
          </div>

          <div className={styles.resultRow}>
            {t('dataDiscovery.datasetsTable.resultCount', {
              count: pagination.totalCount ?? 0,
            })}
          </div>

          <div className={styles.table}>
            <Spin spinning={dataListFetchLoading}>
              <Table
                rowKey="id"
                columns={columns}
                dataSource={datasetList}
                size="small"
                scroll={scroll}
                pagination={tablePagination}
              />
            </Spin>
          </div>
        </div>
      </Card>
    </div>
  );
}
