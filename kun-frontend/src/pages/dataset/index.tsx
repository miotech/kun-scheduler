import React, { useCallback, useMemo } from 'react';
import { useHistory, Link } from 'umi';
import { Input, Select, Table, Tag, Spin } from 'antd';
import { ColumnProps } from 'antd/es/table';
import { PaginationProps } from 'antd/es/pagination';
import { useDispatch, useSelector, shallowEqual } from 'react-redux';
import { useUpdateEffect, useMount } from 'ahooks';
import qs from 'qs';
import { CopyOutlined } from '@ant-design/icons';
import { RootDispatch, RootState } from '@/rematch/store';
import {
  Mode,
  Dataset,
  Watermark,
  GlossaryItem,
} from '@/rematch/models/dataDiscovery';

import color from '@/styles/color';

import useBackPath from '@/hooks/useBackPath';
import useI18n from '@/hooks/useI18n';
import useDebounce from '@/hooks/useDebounce';

import Card from '@/components/Card/Card';
import { watermarkFormatter } from '@/utils/glossaryUtiles';
import TimeSelect from './components/TimeSelect/TimeSelect';

import styles from './index.less';

const { Search } = Input;
const { Option } = Select;

const orderMap = {
  descend: 'desc',
  ascend: 'asc',
};
const tableOrderMap = {
  desc: 'descend',
  asc: 'ascend',
};

const getOrder = (order: keyof typeof tableOrderMap | null) =>
  order ? tableOrderMap[order] : undefined;

export default function DataDisvocery() {
  const t = useI18n();

  const { getBackPath } = useBackPath();

  const history = useHistory();
  const { pathname, search } = history.location;

  const dispatch = useDispatch<RootDispatch>();
  const {
    searchContent,
    watermarkMode,
    watermarkAbsoluteValue,
    watermarkQuickeValue,

    dsTypeList,
    ownerList,
    tagList,
    dsIdList,
    dbList,
    glossaryIdList,

    sortKey,
    sortOrder,

    allDbList,
    allOwnerList,
    allTagList,
    allDsList,
    allGlossaryList,

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

      dsTypeList: state.dataDiscovery.dsTypeList,
      ownerList: state.dataDiscovery.ownerList,
      tagList: state.dataDiscovery.tagList,
      dsIdList: state.dataDiscovery.dsIdList,
      dbList: state.dataDiscovery.dbList,
      glossaryIdList: state.dataDiscovery.glossaryIdList,

      sortKey: state.dataDiscovery.sortKey,
      sortOrder: state.dataDiscovery.sortOrder,

      allDbList: state.dataDiscovery.allDbList,
      allOwnerList: state.dataDiscovery.allOwnerList,
      allTagList: state.dataDiscovery.allTagList,
      allDsList: state.dataDiscovery.allDsList,
      allGlossaryList: state.dataDiscovery.allGlossaryList,

      datasetList: state.dataDiscovery.datasetList,
      pagination: state.dataDiscovery.pagination,
      dataListFetchLoading: state.dataDiscovery.dataListFetchLoading,
      databaseTypes: state.dataSettings.databaseTypeFieldMapList,
    }),
    shallowEqual,
  );

  const debounceSearchContent = useDebounce(searchContent, 1000);

  const fetchDatasets = useCallback(() => {
    dispatch.dataDiscovery.searchDatasets({
      searchContent: debounceSearchContent,
      ownerList,
      tagList,
      dsTypeList,
      dsIdList,
      dbList,
      glossaryIdList,
      watermarkMode,
      watermarkAbsoluteValue,
      watermarkQuickeValue,
      sortKey,
      sortOrder,
      pagination: {
        pageSize: pagination.pageSize,
        pageNumber: pagination.pageNumber || 1,
      },
    });
  }, [
    dispatch.dataDiscovery,
    debounceSearchContent,
    ownerList,
    tagList,
    dsTypeList,
    dsIdList,
    dbList,
    glossaryIdList,
    watermarkMode,
    watermarkAbsoluteValue,
    watermarkQuickeValue,
    sortKey,
    sortOrder,
    pagination.pageSize,
    pagination.pageNumber,
  ]);

  useMount(() => {
    if (search) {
      const oldFilters = qs.parse(search.replace('?', ''));
      dispatch.dataDiscovery.updateFilterAndPaginationFromUrl(oldFilters);
    }
    fetchDatasets();
    dispatch.dataDiscovery.fetchAllOwnerList();
    dispatch.dataDiscovery.fetchAllTagList();
    dispatch.dataSettings.fetchDatabaseTypeList();
    dispatch.dataDiscovery.fetchAllDs('');
    dispatch.dataDiscovery.fetchAllDb();
    dispatch.dataDiscovery.fetchAllGlossary();
  });

  useUpdateEffect(() => {
    fetchDatasets();
  }, [fetchDatasets]);

  const currentUrl = useMemo(() => {
    const shouldFilter = {
      searchContent,
      watermarkMode,
      watermarkAbsoluteValue,
      watermarkQuickeValue,

      dsTypeList,
      ownerList,
      tagList,
      dsIdList,
      dbList,
      glossaryIdList,

      sortKey,
      sortOrder,

      pagination,
    };

    const currentQuery = qs.stringify(shouldFilter);
    return `${pathname}?${currentQuery}`;
  }, [
    dbList,
    dsIdList,
    dsTypeList,
    glossaryIdList,
    ownerList,
    pagination,
    pathname,
    searchContent,
    sortKey,
    sortOrder,
    tagList,
    watermarkAbsoluteValue,
    watermarkMode,
    watermarkQuickeValue,
  ]);

  // 更新url
  useUpdateEffect(() => {
    history.replace(currentUrl);
  }, [currentUrl, history]);

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
          sorter: true,
          width: 170,
          defaultSortOrder:
            sortKey === 'name' ? getOrder(sortOrder) : undefined,
          render: (name: string) => (
            <span className={styles.nameLink}>{name}</span>
          ),
        },
        {
          title: t('dataDiscovery.datasetsTable.header.database'),
          dataIndex: 'database',
          key: 'database_name',
          sorter: true,
          width: 80,
          defaultSortOrder:
            sortKey === 'database_name' ? getOrder(sortOrder) : undefined,
        },
        {
          title: t('dataDiscovery.datasetsTable.header.datasource'),
          dataIndex: 'datasource',
          key: 'datasource_name',
          sorter: true,
          width: 120,
          defaultSortOrder:
            sortKey === 'datasource_name' ? getOrder(sortOrder) : undefined,
        },
        {
          title: t('dataDiscovery.datasetsTable.header.dbtype'),
          dataIndex: 'type',
          key: 'type',
          sorter: true,
          width: 80,
          defaultSortOrder:
            sortKey === 'type' ? getOrder(sortOrder) : undefined,
        },
        {
          title: t('dataDiscovery.datasetsTable.header.watermark'),
          dataIndex: 'high_watermark',
          key: 'high_watermark',
          sorter: true,
          defaultSortOrder:
            sortKey === 'high_watermark' ? getOrder(sortOrder) : undefined,
          width: 150,
          render: (watermark: Watermark) => watermarkFormatter(watermark.time),
        },
        {
          title: t('dataDiscovery.datasetsTable.header.glossary'),
          dataIndex: 'glossaries',
          key: 'glossaries',
          width: 180,
          render: (glossaties: GlossaryItem[]) => (
            <div
              onClick={e => {
                e.stopPropagation();
              }}
            >
              {(glossaties || []).slice(0, 3).map(glossary => (
                <div key={glossary.id} className={styles.glossaryItem}>
                  <CopyOutlined className={styles.glossaryIcon} />
                  <Link
                    to={getBackPath(`/data-discovery/glossary/${glossary.id}`)}
                  >
                    {glossary.name}
                  </Link>
                </div>
              ))}
              {glossaties && glossaties.length > 3 && (
                <div className={styles.ellipsisItem}>...</div>
              )}
            </div>
          ),
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
    [getBackPath, sortKey, sortOrder, t],
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

  const handleChangeTable = useCallback(
    (_pagination, _filters, sorter) => {
      const { columnKey, order } = sorter;
      dispatch.dataDiscovery.updateFilter({ key: 'sortKey', value: columnKey });
      dispatch.dataDiscovery.updateFilter({
        key: 'sortOrder',
        value: order ? orderMap[order as 'descend' | 'ascend'] : null,
      });
    },
    [dispatch.dataDiscovery],
  );

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
                  optionFilterProp="children"
                  onChange={v => {
                    dispatch.dataDiscovery.updateFilter({
                      key: 'dsIdList',
                      value: v,
                    });
                  }}
                  placeholder={t('dataDiscovery.pleaseSelect')}
                  allowClear
                >
                  {allDsList.map(option => (
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
                  value={dsTypeList}
                  mode="multiple"
                  size="large"
                  optionFilterProp="children"
                  onChange={v => {
                    dispatch.dataDiscovery.updateFilter({
                      key: 'dsTypeList',
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

            <div className={styles.filterItem}>
              <div className={styles.filterItemTitle}>
                {t('dataDiscovery.glossary')}
              </div>
              <div className={styles.filterItemSelect}>
                <Select
                  value={glossaryIdList}
                  mode="multiple"
                  size="large"
                  optionFilterProp="children"
                  onChange={v => {
                    dispatch.dataDiscovery.updateFilter({
                      key: 'glossaryIdList',
                      value: v,
                    });
                  }}
                  placeholder={t('dataDiscovery.pleaseSelect')}
                  allowClear
                >
                  {allGlossaryList.map(glossary => (
                    <Option key={glossary.id} value={glossary.id}>
                      {glossary.name}
                    </Option>
                  ))}
                </Select>
              </div>
            </div>

            <div className={styles.filterItem}>
              <div className={styles.filterItemTitle}>
                {t('dataDiscovery.db')}
              </div>
              <div className={styles.filterItemSelect}>
                <Select
                  value={dbList}
                  mode="multiple"
                  size="large"
                  optionFilterProp="children"
                  onChange={v => {
                    dispatch.dataDiscovery.updateFilter({
                      key: 'dbList',
                      value: v,
                    });
                  }}
                  placeholder={t('dataDiscovery.pleaseSelect')}
                  allowClear
                >
                  {allDbList.map(db => (
                    <Option key={db.name} value={db.name}>
                      {db.name}
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
                onChange={handleChangeTable}
                onRow={record => ({
                  onClick: () => {
                    const url = encodeURIComponent(
                      `${history.location.pathname}${history.location.search}`,
                    );
                    history.push(
                      `/data-discovery/dataset/${record.id}?backUrl=${url}`,
                    );
                  },
                  style: {
                    cursor: 'pointer',
                  },
                })}
              />
            </Spin>
          </div>
        </div>
      </Card>
    </div>
  );
}
