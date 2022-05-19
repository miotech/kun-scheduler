import React, { useCallback, useEffect, useMemo } from 'react';
import { useHistory, Link } from 'umi';
import { Input, Table, Tag, Spin, Select } from 'antd';
import { ColumnProps } from 'antd/es/table';
import { PaginationProps } from 'antd/es/pagination';
import { useDispatch, useSelector, shallowEqual } from 'react-redux';
import { useUpdateEffect, useMount } from 'ahooks';
import { useQueryParams, StringParam, NumberParam, withDefault, BooleanParam } from 'use-query-params';
import { CopyOutlined } from '@ant-design/icons';
import { RootDispatch, RootState } from '@/rematch/store';
import { SearchParams, QueryObj } from '@/rematch/models/dataDiscovery';
import {
  Dataset,
  GlossaryItem,
  UpstreamTask,
  QueryAttributeListBody,
  ResourceAttributeMap,
} from '@/definitions/Dataset.type';
import { queryAttributeList } from '@/services/dataDiscovery';
import color from '@/styles/color';
import useBackPath from '@/hooks/useBackPath';
import useI18n from '@/hooks/useI18n';
import useDebounce from '@/hooks/useDebounce';

import Card from '@/components/Card/Card';
import getEllipsisString from '@/utils/getEllipsisString';

import styles from './index.less';

const { Search } = Input;
const { Option } = Select;

function computeRowClassname(record: Dataset): string {
  if (record.deleted) {
    return styles.datasetDeleted;
  }
  // else
  return '';
}

export default function DataDiscoveryListView() {
  const t = useI18n();

  const { getBackPath } = useBackPath();

  const history = useHistory();
  const [query, setQuery] = useQueryParams({
    searchContent: StringParam,
    datasource: StringParam,
    database: StringParam,
    schema: StringParam,
    type: StringParam,
    tags: StringParam,
    owners: StringParam,
    pageNumber: withDefault(NumberParam, 1),
    pageSize: withDefault(NumberParam, 15),
    displayDeleted: BooleanParam,
  });

  const { searchContent, datasource, database, schema, type, tags, owners, pageNumber, pageSize } = query as QueryObj;

  const dispatch = useDispatch<RootDispatch>();
  const { pagination, datasetList, dataListFetchLoading, selectOptions } = useSelector(
    (state: RootState) => ({
      pagination: state.dataDiscovery.pagination,
      datasetList: state.dataDiscovery.datasetList,
      dataListFetchLoading: state.dataDiscovery.dataListFetchLoading,
      selectOptions: state.dataDiscovery.selectOptions,
    }),
    shallowEqual,
  );

  const debounceSearchContent = useDebounce(searchContent, 1000);

  const fetchDatasets = useCallback(() => {
    dispatch.dataDiscovery.searchDatasets({
      searchContent: debounceSearchContent,
      datasource,
      database,
      schema,
      type,
      tags,
      owners,
      pagination: {
        pageSize: pageSize || 15,
        pageNumber: pageNumber || 1,
      },
    } as SearchParams);
  }, [
    dispatch.dataDiscovery,
    debounceSearchContent,
    datasource,
    database,
    schema,
    type,
    tags,
    owners,
    pageSize,
    pageNumber,
  ]);

  useMount(() => {
    fetchDatasets();
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

  const getAttributeList = useCallback(
    async (key: ResourceAttributeMap) => {
      const params: QueryAttributeListBody = {
        resourceAttributeName: key,
        resourceAttributeMap: {
          datasource,
          database,
          schema,
          type,
          tags,
          owners,
        },
      };
      params.resourceAttributeMap[key] = null;
      const res = await queryAttributeList(params);
      if (res) {
        dispatch.dataDiscovery.updateOptions({ key, value: res });
      }
    },
    [datasource, database, schema, type, tags, owners, dispatch],
  );

  useEffect(() => {
    getAttributeList(ResourceAttributeMap.datasource);
    getAttributeList(ResourceAttributeMap.database);
    getAttributeList(ResourceAttributeMap.schema);
    getAttributeList(ResourceAttributeMap.type);
    getAttributeList(ResourceAttributeMap.tags);
    getAttributeList(ResourceAttributeMap.owners);
  }, [query]);
  const handleChangeSearch = useCallback(
    e => {
      setFilterQuery({ searchContent: e.target.value });
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
          fixed: 'left',
          width: 170,
          render: (name: string) => <span className={styles.nameLink}>{name}</span>,
        },
        {
          title: t('dataDiscovery.datasetsTable.header.database'),
          dataIndex: 'database',
          key: 'databaseName',
          width: 80,
        },
        {
          title: t('dataDiscovery.datasetsTable.header.datasource'),
          dataIndex: 'datasource',
          key: 'datasourceName',
          width: 120,
        },
        {
          title: t('dataDiscovery.datasetsTable.header.dbtype'),
          dataIndex: 'type',
          key: 'type',
          width: 80,
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
                  <Link to={getBackPath(`/data-discovery/glossary?glossaryId=${glossary.id}`)}>{glossary.name}</Link>
                </div>
              ))}
              {glossaties && glossaties.length > 3 && <div className={styles.ellipsisItem}>...</div>}
            </div>
          ),
        },
        {
          title: t('dataDiscovery.datasetsTable.header.description'),
          dataIndex: 'description',
          key: 'description',
          width: 300,
          render: (description: string) => (
            <div className={styles.descriptionColumn} title={description}>
              {getEllipsisString(description, 50, 30, 0)}
            </div>
          ),
        },
        {
          title: t('dataDiscovery.datasetsTable.header.owners'),
          dataIndex: 'owners',
          key: 'owners',
          width: 300,
          render: (item: string[]) => (
            <>
              {(item || []).map(owner => (
                <Tag key={owner} className="light-blue-tag" color={color.lightBlue}>
                  {owner}
                </Tag>
              ))}
            </>
          ),
        },
        {
          title: t('dataDiscovery.datasetsTable.header.upstream'),
          dataIndex: 'upstreamTasks',
          key: 'upstreamTasks',
          width: 300,
          render: (upstreamTasks: UpstreamTask[]) => (
            <>
              {(upstreamTasks || []).map(upstreamTask => (
                <div
                  onClick={e => {
                    e.stopPropagation();
                  }}
                >
                  <Link to={`/data-development/task-definition/${upstreamTask.definitionId}`}>{upstreamTask.name}</Link>
                </div>
              ))}
            </>
          ),
        },
        {
          title: t('dataDiscovery.datasetsTable.header.tags'),
          dataIndex: 'tags',
          key: 'tags',
          width: 300,
          render: (tag: string[]) => (
            <>
              {(tag || []).map(item => (
                <Tag key={item} className="light-blue-tag" color={color.lightBlue}>
                  {item}
                </Tag>
              ))}
            </>
          ),
        },
      ] as ColumnProps<Dataset>[],
    [getBackPath, t],
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
    [handleChangePage, handleChangePageSize, pageNumber, pageSize, pagination.totalCount],
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
          <div className={styles.resultRow}>
            {t('dataDiscovery.datasetsTable.resultCount', {
              count: pagination.totalCount ?? 0,
            })}
          </div>

          <div className={styles.filterRow}>
            {Object.keys(ResourceAttributeMap).map(key => (
              <div className={styles.filterItem} key={key}>
                <div className={styles.filterItemTitle}>{t(`dataDiscovery.${key}`)}</div>
                <div className={styles.filterItemSelect}>
                  <Select
                    value={query[key]}
                    onChange={v => {
                      setFilterQuery({ [key]: v });
                    }}
                    showSearch
                    placeholder={t('dataDiscovery.pleaseSelect')}
                    allowClear
                  >
                    {selectOptions[key as ResourceAttributeMap].map(option => (
                      <Option key={option} value={option}>
                        {option}
                      </Option>
                    ))}
                  </Select>
                </div>
              </div>
            ))}
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
                rowClassName={computeRowClassname}
                onRow={record => ({
                  onClick: () => {
                    const url = encodeURIComponent(`${history.location.pathname}${history.location.search}`);
                    history.push(`/data-discovery/dataset/${record.id}?backUrl=${url}`);
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
