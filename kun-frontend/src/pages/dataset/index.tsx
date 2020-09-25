import React, { useCallback, useMemo } from 'react';
import { useHistory, Link } from 'umi';
import { Input, Select, Table, Tag, Spin } from 'antd';
import { ColumnProps } from 'antd/es/table';
import { PaginationProps } from 'antd/es/pagination';
import { useDispatch, useSelector, shallowEqual } from 'react-redux';
import { useUpdateEffect, useMount } from 'ahooks';
import {
  useQueryParams,
  StringParam,
  NumberParam,
  ArrayParam,
  ObjectParam,
  withDefault,
} from 'use-query-params';
import { CopyOutlined } from '@ant-design/icons';
import { RootDispatch, RootState } from '@/rematch/store';
import {
  Mode,
  Dataset,
  Watermark,
  GlossaryItem,
  SearchParams,
  QueryObj,
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

  const [query, setQuery] = useQueryParams({
    searchContent: StringParam,
    watermarkMode: withDefault(StringParam, Mode.ABSOLUTE),
    watermarkAbsoluteValue: ObjectParam,
    watermarkQuickeValue: StringParam,

    dsTypeList: ArrayParam,
    ownerList: ArrayParam,
    tagList: ArrayParam,
    dsIdList: ArrayParam,
    dbList: ArrayParam,
    glossaryIdList: ArrayParam,

    sortKey: StringParam,
    sortOrder: StringParam,

    pageNumber: withDefault(NumberParam, 1),
    pageSize: withDefault(NumberParam, 15),
  });

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
    pageNumber,
    pageSize,
  } = query as QueryObj;

  const dispatch = useDispatch<RootDispatch>();
  const {
    pagination,

    allDbList,
    allOwnerList,
    allTagList,
    allDsList,
    allGlossaryList,

    datasetList,

    dataListFetchLoading,
    databaseTypes,
  } = useSelector(
    (state: RootState) => ({
      pagination: state.dataDiscovery.pagination,

      allDbList: state.dataDiscovery.allDbList,
      allOwnerList: state.dataDiscovery.allOwnerList,
      allTagList: state.dataDiscovery.allTagList,
      allDsList: state.dataDiscovery.allDsList,
      allGlossaryList: state.dataDiscovery.allGlossaryList,

      datasetList: state.dataDiscovery.datasetList,
      dataListFetchLoading: state.dataDiscovery.dataListFetchLoading,
      databaseTypes: state.dataSettings.databaseTypeFieldMapList,
    }),
    shallowEqual,
  );

  const debounceSearchContent = useDebounce(searchContent, 1000);

  const fetchDatasets = useCallback(() => {
    dispatch.dataDiscovery.searchDatasets({
      searchContent: debounceSearchContent || '',
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
        pageSize: pageSize || 15,
        pageNumber: pageNumber || 1,
      },
    } as SearchParams);
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
    pageSize,
    pageNumber,
  ]);

  useMount(() => {
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

  const setFilterQuery = useCallback(
    (obj, shouldChangePageNum = true) => {
      if (shouldChangePageNum) {
        setQuery({ ...obj, pageNumber: 1 }, 'replaceIn');
      } else {
        setQuery(obj, 'replaceIn');
      }
    },
    [setQuery],
  );

  const handleChangeSearch = useCallback(
    e => {
      setFilterQuery({ searchContent: e.target.value });
    },
    [setFilterQuery],
  );

  const handleChangeWatermarkMode = useCallback(
    mode => {
      setFilterQuery({ watermarkMode: mode });
    },
    [setFilterQuery],
  );

  const timeSelectValue = useMemo(
    () =>
      watermarkMode === Mode.ABSOLUTE
        ? {
            startTime: watermarkAbsoluteValue?.startTime
              ? Number(watermarkAbsoluteValue.startTime)
              : null,
            endTime: watermarkAbsoluteValue?.endTime
              ? Number(watermarkAbsoluteValue.endTime)
              : null,
          }
        : watermarkQuickeValue,
    [watermarkMode, watermarkAbsoluteValue, watermarkQuickeValue],
  );

  const handleChangeWatermarkValue = useCallback(
    (v, mode) => {
      if (mode === Mode.ABSOLUTE) {
        setFilterQuery({ watermarkAbsoluteValue: v });
      }
      if (mode === Mode.QUICK) {
        setFilterQuery({ watermarkQuickeValue: v });
      }
    },
    [setFilterQuery],
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
    (pageNum: number) => {
      setFilterQuery({ pageNumber: pageNum }, false);
    },
    [setFilterQuery],
  );

  const handleChangePageSize = useCallback(
    (_pageNumber, currentpageSize) => {
      setFilterQuery({ pageSize: currentpageSize });
    },
    [setFilterQuery],
  );

  const tablePagination: PaginationProps = useMemo(
    () => ({
      size: 'small',
      total: pagination.totalCount,
      current: pageNumber,
      pageSize,
      pageSizeOptions: ['15', '25', '50', '100'],
      onChange: handleChangePage,
      onShowSizeChange: handleChangePageSize,
    }),
    [
      handleChangePage,
      handleChangePageSize,
      pageNumber,
      pageSize,
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
      if (
        columnKey &&
        order &&
        (columnKey !== sortKey ||
          orderMap[order as keyof typeof orderMap] !== sortOrder)
      ) {
        setFilterQuery({
          sortKey: columnKey,
          sortOrder: order ? orderMap[order as 'descend' | 'ascend'] : null,
        });
      }
      if (columnKey && !order) {
        setFilterQuery({
          sortKey: columnKey,
          sortOrder: null,
        });
      }
    },
    [setFilterQuery, sortKey, sortOrder],
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
                  mode={watermarkMode || Mode.ABSOLUTE}
                  onModeChange={handleChangeWatermarkMode}
                  value={timeSelectValue}
                  onChange={handleChangeWatermarkValue}
                />
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
                    setFilterQuery({ dbList: v });
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
                    setFilterQuery({ dsIdList: v });
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
                    setFilterQuery({ dsTypeList: v });
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
                {t('dataDiscovery.glossary')}
              </div>
              <div className={styles.filterItemSelect}>
                <Select
                  value={glossaryIdList}
                  mode="multiple"
                  size="large"
                  optionFilterProp="children"
                  onChange={v => {
                    setFilterQuery({ glossaryIdList: v });
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
                {t('dataDiscovery.owners')}
              </div>
              <div className={styles.filterItemSelect}>
                <Select
                  value={ownerList}
                  mode="multiple"
                  size="large"
                  onChange={v => {
                    setFilterQuery({ ownerList: v });
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
                    setFilterQuery({ tagList: v });
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
