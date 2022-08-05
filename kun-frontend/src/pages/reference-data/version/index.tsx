import React, { useEffect, useState, useCallback, useMemo } from 'react';
import { useRequest } from 'ahooks';
import { Table, Tag, Popconfirm, Checkbox } from 'antd';
import { dateFormatter } from '@/utils/datetime-utils';
import { getVersionList, deactivateVersionAPI, rollbackVersionAPI } from '@/services/reference-data/referenceData';
import useUrlState from '@ahooksjs/use-url-state';
import { ReferenceRecords, ShowStatus } from '@/definitions/ReferenceData.type';
import useI18n from '@/hooks/useI18n';
import { PaginationProps } from 'antd/es/pagination';
import { useQueryParams, NumberParam, withDefault } from 'use-query-params';
import styles from './index.less';
import VersionDetail from './versionDetail';

export default function DatabaseTableConfiguration() {
  const [routeState] = useUrlState();
  const t = useI18n();
  const i18n = 'dataDiscovery.referenceData.version';
  const [versionId, setVersionId] = useState<string>('');
  const { data: versionList, loading, run: doSearch } = useRequest(getVersionList, {
    manual: true,
  });
  useEffect(() => {
    doSearch(routeState.tableId);
  }, [routeState.tableId, doSearch]);

  const [query, setQuery] = useQueryParams({
    pageNumber: withDefault(NumberParam, 1),
    pageSize: withDefault(NumberParam, 15),
  });

  const { pageNumber, pageSize } = query;

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
      total: versionList?.length || 0,
      current: pageNumber,
      pageSize,
      pageSizeOptions: ['15', '25', '50', '100'],
      onChange: handleChangePage,
      onShowSizeChange: handleChangePageSize,
      showSizeChanger: true,
    }),
    [versionList, handleChangePage, handleChangePageSize, pageNumber, pageSize],
  );

  const rollbackVersion = useCallback(
    async (id: string) => {
      const res = await rollbackVersionAPI(id);
      if (res) {
        doSearch(routeState.tableId);
      }
    },
    [doSearch, routeState.tableId],
  );

  const deactivateVersion = useCallback(
    async (id: string) => {
      const res = await deactivateVersionAPI(id);
      if (res) {
        doSearch(routeState.tableId);
      }
    },
    [doSearch, routeState.tableId],
  );
  const columns = [
    {
      title: t(`${i18n}.table.version`),
      dataIndex: 'versionNumber',
      key: 'versionNumber',
      render: (versionNumber: number, record: ReferenceRecords) => {
        return <a onClick={() => setVersionId(record.versionId)}>{versionNumber ? `v${versionNumber}` : '—'}</a>;
      },
    },
    {
      title: t(`${i18n}.table.utime`),
      dataIndex: 'updateTime',
      key: 'updateTime',
      render: (updateTime: string) => dateFormatter(updateTime),
    },
    {
      title: t(`${i18n}.table.updateby`),
      dataIndex: 'updateUser',
      key: 'updateUser',
    },

    {
      title: t(`${i18n}.table.ctime`),
      dataIndex: 'createTime',
      key: 'createTime',
      render: (createTime: string) => dateFormatter(createTime),
    },
    {
      title: t(`${i18n}.table.owner`),
      dataIndex: 'ownerList',
      key: 'ownerList',
      render: (ownerList: string[]) => {
        return ownerList?.map((owner: string) => <Tag>{owner}</Tag>);
      },
    },

    {
      title: t(`${i18n}.table.status`),
      dataIndex: 'showStatus',
      key: 'showStatus',
      render: (showStatus: ShowStatus, record: ReferenceRecords) => {
        if (showStatus === ShowStatus.UNPUBLISHED) {
          return <Checkbox disabled> {t('dataDiscovery.referenceData.status.saved')}</Checkbox>;
        }
        if (showStatus === ShowStatus.PUBLISHED) {
          return (
            <Popconfirm
              title={t('dataDiscovery.referenceData.notice.deactivatedVersion')}
              onConfirm={() => deactivateVersion(record.versionId)}
            >
              <Checkbox checked>{t('dataDiscovery.referenceData.status.released')} </Checkbox>
            </Popconfirm>
          );
        }
        if (showStatus === ShowStatus.HISTORY) {
          return (
            <Popconfirm
              title={t('dataDiscovery.referenceData.notice.rollbackVersion')}
              onConfirm={() => rollbackVersion(record.versionId)}
            >
              <Checkbox checked={false}>{t('dataDiscovery.referenceData.status.deactivated')} </Checkbox>
            </Popconfirm>
          );
        }
        return <></>;
      },
    },
  ];

  return (
    <div className={styles.content}>
      <div className={styles.header}>
        <div className={styles.title}>
          {t(`${i18n}.title`)} - {routeState.tableName}
        </div>
      </div>
      <div className={styles.table}>
        <Table
          rowKey="versionId"
          columns={columns}
          pagination={tablePagination}
          dataSource={versionList}
          size="middle"
          loading={loading}
        />
      </div>
      <VersionDetail versionId={versionId} setVersionId={setVersionId} />
    </div>
  );
}
