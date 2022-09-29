import React, { useCallback, useState, useMemo } from 'react';
import { Tag, Table, Popover } from 'antd';
import { pickBy } from 'lodash';
import Icon, { PlusOutlined } from '@ant-design/icons';
import { PaginationProps } from 'antd/es/pagination';
import { ReactComponent as UploadExcelIcon } from '@/assets/icons/upload-excel.svg';
import { fetchRdmDatas } from '@/services/reference-data/referenceData';
import { useRequest } from 'ahooks';
import { Link } from 'umi';
import { dateFormatter } from '@/utils/datetime-utils';
import { GlossaryChild } from '@/rematch/models/glossary';
import { ReferenceRecords, LineageDataset, FetchRdmDatasParams } from '@/definitions/ReferenceData.type';
import useI18n from '@/hooks/useI18n';
import styles from './index.less';
import UploadModal from './components/Upload';
import TimeCited from './components/TimeCited';
import { FilterBar, Filters } from './components';

const CreatePopoverContent = (setIsModalVisible: (value: boolean) => void) => {
  const t = useI18n();
  return (
    <div className={styles.pophover}>
      <div className={styles.item} onClick={() => setIsModalVisible(true)}>
        <Icon className={styles.icon} component={UploadExcelIcon} />
        <div className={styles.title}> {t('dataDiscovery.referenceData.uploadExcel')}</div>
      </div>
    </div>
  );
};
export default function ReferenceData() {
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [timeCitedVisible, setTimeCitedVisible] = useState(false);
  const [currentTableName, setCurrentName] = useState<string>();
  const [timeCited, setTimeCited] = useState<LineageDataset[]>();

  const t = useI18n();
  const i18n = 'dataDiscovery.referenceData.table';

  const [query, setQuery] = useState<FetchRdmDatasParams>({
    pageNumber: 1,
    pageSize: 15,
  });

  const { data: tableList, loading } = useRequest(
    fetchRdmDatas.bind(null, pickBy(query, value => !!value) as FetchRdmDatasParams),
    {
      refreshDeps: [query],
      debounceTrailing: true,
      debounceWait: 500,
    },
  );

  const { pageNumber, pageSize } = query;

  const setFilterQuery = useCallback((filterQuery, shouldChangePageNum = true) => {
    let updatedQuery = { ...filterQuery };
    if (shouldChangePageNum) {
      updatedQuery = {
        ...updatedQuery,
        pageNumber: 1,
      };
    }
    setQuery((preQuery: FetchRdmDatasParams) => ({
      ...preQuery,
      ...updatedQuery,
    }));
  }, []);

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

  const openCited = useCallback((record: ReferenceRecords) => {
    const { tableName, lineageDatasetList } = record;
    if (lineageDatasetList?.length) {
      setCurrentName(tableName);
      setTimeCited(lineageDatasetList);
      setTimeCitedVisible(true);
    }
  }, []);

  const handleFilterChange = useCallback(
    (updatedFilters: Filters) => {
      setFilterQuery(updatedFilters);
    },
    [setFilterQuery],
  );

  const tablePagination: PaginationProps = useMemo(
    () => ({
      size: 'small',
      total: tableList?.totalCount || 0,
      current: pageNumber,
      pageSize,
      pageSizeOptions: ['15', '25', '50', '100'],
      onChange: handleChangePage,
      onShowSizeChange: handleChangePageSize,
      showSizeChanger: true,
    }),
    [tableList, handleChangePage, handleChangePageSize, pageNumber, pageSize],
  );

  const columns = [
    {
      title: t(`${i18n}.tableName`),
      dataIndex: 'tableName',
      key: 'tableName',
      render: (name: string, record: ReferenceRecords) => {
        return (
          <div style={{ maxWidth: '200px' }}>
            <Link to={`/data-discovery/reference-data/table-configration?tableId=${record.tableId}`}>{name}</Link>
          </div>
        );
      },
    },
    {
      title: t(`${i18n}.description`),
      dataIndex: 'versionDescription',
      key: 'versionDescription',
      render: (versionDescription: string) => {
        return <div style={{ maxWidth: '200px' }}>{versionDescription}</div>;
      },
    },

    {
      title: t(`${i18n}.glossary`),
      dataIndex: 'glossaryList',
      key: 'glossaryList',
      render: (glossaryList: GlossaryChild[]) => {
        return (
          <div style={{ maxWidth: '200px' }}>
            {glossaryList?.map((glossary: GlossaryChild) => (
              <Tag style={{ marginTop: '5px' }}>{glossary.name}</Tag>
            ))}
          </div>
        );
      },
    },
    {
      title: t(`${i18n}.version`),
      dataIndex: 'versionNumber',
      key: 'versionNumber',
      render: (versionNumber: number, record: ReferenceRecords) => {
        const content = (
          <Link to={`/data-discovery/reference-data/version?tableId=${record.tableId}&tableName=${record.tableName}`}>
            {t(`${i18n}.viewHistoryVersion`)}
          </Link>
        );
        return (
          <Popover content={content}>
            <div className={styles.link}>{versionNumber ? `v ${versionNumber}` : '-'}</div>
          </Popover>
        );
      },
    },

    {
      title: t(`${i18n}.owner`),
      dataIndex: 'ownerList',
      key: 'ownerList',
      render: (ownerList: string[]) => {
        return ownerList?.map((owner: string) => <Tag>{owner}</Tag>);
      },
    },
    {
      title: t(`${i18n}.ctime`),
      dataIndex: 'createTime',
      key: 'createTime',
      render: (createTime: string) => dateFormatter(createTime),
    },
    {
      title: t(`${i18n}.updateby`),
      dataIndex: 'updateUser',
      key: 'updateUser',
    },

    {
      title: t(`${i18n}.utime`),
      dataIndex: 'updateTime',
      key: 'updateTime',
      render: (updateTime: string) => dateFormatter(updateTime),
    },
    {
      title: t(`${i18n}.timeCited`),
      dataIndex: 'lineageDatasetList',
      key: 'lineageDatasetList',
      render: (lineageDatasetList: string[], record: ReferenceRecords) => {
        return (
          <div className={styles.link} onClick={() => openCited(record)}>
            {lineageDatasetList?.length || '-'}
          </div>
        );
      },
    },
  ];
  return (
    <div className={styles.content}>
      <div className={styles.header}>
        <Popover placement="bottomRight" content={() => CreatePopoverContent(setIsModalVisible)} trigger="click">
          <PlusOutlined className={styles.addButton} />
        </Popover>
      </div>
      <FilterBar
        filters={query as Filters}
        totalCount={tablePagination.total || 0}
        onFilterChange={handleFilterChange}
      />
      <div className={styles.table}>
        <Table
          columns={columns}
          dataSource={tableList?.records}
          size="middle"
          loading={loading}
          rowKey="versionId"
          pagination={tablePagination}
        />
      </div>
      {isModalVisible && <UploadModal isModalVisible={isModalVisible} setIsModalVisible={setIsModalVisible} />}

      <TimeCited
        currentTableName={currentTableName}
        timeCited={timeCited}
        timeCitedVisible={timeCitedVisible}
        setTimeCitedVisible={setTimeCitedVisible}
      />
    </div>
  );
}
