import React, { useEffect, useState, useCallback, useMemo } from 'react';
import c from 'clsx';
import { Link } from 'umi';
import { useQueryParams, StringParam } from 'use-query-params';
import { RouteComponentProps } from 'react-router';
import numeral from 'numeral';
import { FileTextOutlined } from '@ant-design/icons';
import { Spin, Button, message, Select, Input, Divider, Table } from 'antd';
import { TablePaginationConfig } from 'antd/lib/table';
import Card from '@/components/Card/Card';

import { watermarkFormatter } from '@/utils/glossaryUtiles';

import useI18n from '@/hooks/useI18n';
import useRedux from '@/hooks/useRedux';
import useDebounce from '@/hooks/useDebounce';
import BackButton from '@/components/BackButton/BackButton';
import useBackPath from '@/hooks/useBackPath';
import { Watermark } from '@/definitions/Dataset.type';
import { Column } from '@/rematch/models/datasetDetail';
import { LineageDirection } from '@/services/lineage';

import { DatasetPullProcessVO } from '@/services/datasetDetail';
import { useInterval } from 'ahooks';
import DescriptionInput from './components/DescriptionInput/DescriptionInput';
import ColumnDescInput from './components/ColumnDescInput/ColumnDescInput';
import AddDataQualityModal from './components/AddDataQualityModal/AddDataQualityModal';
import DataQualityTable from './components/DataQualityTable/DataQualityTable';
import LineageStreamTaskTable from './components/LineageStreamTaskTable/LineageStreamTaskTable';

import styles from './index.less';

interface MatchParams {
  datasetId: string;
}

interface Props extends RouteComponentProps<MatchParams> {}

const { Option } = Select;

function isPullingStatus(pullProcess: DatasetPullProcessVO | null) {
  if (pullProcess == null || pullProcess?.latestMCETaskRun?.status == null) {
    return false;
  }
  // else
  if (pullProcess.latestMCETaskRun.status === 'SUCCESS' ||
    pullProcess.latestMCETaskRun.status === 'FAILED' ||
    pullProcess.latestMCETaskRun.status === 'ABORTED') {
    return false;
  }
  // else
  return true;
}

export default function DatasetDetail({ match }: Props) {
  const [query] = useQueryParams({
    caseId: StringParam,
  });
  const t = useI18n();
  const { getBackPath } = useBackPath();

  const { selector, dispatch } = useRedux(state => state.datasetDetail);
  const {
    selector: { allOwnerList, allTagList, latestPullProcess, latestPullProcessIsLoading, deleted },
  } = useRedux(state => ({
    allOwnerList: state.dataDiscovery.allOwnerList,
    allTagList: state.dataDiscovery.allTagList,
    latestPullProcess: state.datasetDetail.datasetLatestPullProcess,
    latestPullProcessIsLoading: state.datasetDetail.datasetLatestPullProcessIsLoading,
    /** Is this dataset deleted? */
    deleted: state.datasetDetail.deleted,
  }));

  const debounceColumnKeyword = useDebounce(selector.columnsKeyword, 500);

  const currentId = match.params.datasetId;

  const [fetchDetailLoading, setFetchDetailLoading] = useState(false);
  const [fetchColumnsLoading, setFetchColumnsLoading] = useState(false);
  const [updateLoading, setUpdateLoading] = useState(false);

  const [forceReFetchInfoFlag, setForceReFetchInfoFlag ] = useState(1);
  const [forceUpdateAllTagListFlag, setForceUpdateAllTagListFlag] = useState(1);
  const [
    forceReFetchDataQualityFlag,
    setForceReFetchDataQualityFlag,
  ] = useState(1);

  const [AddDataQualityModalVisible, setAddDataQualityModalVisible] = useState(
    !!query.caseId,
  );

  // 编辑 dataquality 用
  const [currentDataQualityId, setCurrentDataQualityId] = useState<
    string | null
  >(query.caseId || null);

  useEffect(() => {
    // 如果更改了搜索关键词, 那么强制切换页码数为1
    dispatch.datasetDetail.updatePagination({ pageNumber: 1 });
  }, [dispatch.datasetDetail, debounceColumnKeyword, forceReFetchInfoFlag]);

  useEffect(() => {
    async function fetchDatasetData() {
      setFetchColumnsLoading(true);
      try {
        const params = {
          id: currentId,
          keyword: debounceColumnKeyword,
          pagination: {
            pageNumber: selector.columnsPagination.pageNumber,
            pageSize: selector.columnsPagination.pageSize,
          },
        };
        await dispatch.datasetDetail.fetchDatasetColumns(params);
        await dispatch.datasetDetail.fetchDatasetLatestPullProcess(currentId);
      } finally {
        setFetchColumnsLoading(false);
      }
    }
    fetchDatasetData();
  }, [
    currentId,
    debounceColumnKeyword,
    dispatch.datasetDetail,
    selector.columnsPagination.pageNumber,
    selector.columnsPagination.pageSize,
    forceReFetchInfoFlag,
  ]);

  /* If this dataset is still pulling, poll status of the latest process  */
  useInterval(async function pollPullingProcess() {
    const process = await dispatch.datasetDetail.fetchDatasetLatestPullProcess(currentId);
    if (process?.latestMCETaskRun?.status === 'SUCCESS') {
      message.success(t('dataDetail.msg.pullSuccess'));
      setForceReFetchInfoFlag(v => v + 1);
    } else if (process?.latestMCETaskRun?.status === 'FAILED' || process?.latestMCETaskRun?.status === 'ABORTED') {
      message.error(t('dataDetail.msg.pullFailed'));
    }
  }, isPullingStatus(latestPullProcess) ? 3000 : null, { immediate: false });

  useEffect(() => {
    dispatch.dataDiscovery.fetchAllOwnerList();
  }, [dispatch.dataDiscovery]);

  useEffect(() => {
    dispatch.dataDiscovery.fetchAllTagList();
  }, [dispatch.dataDiscovery, forceUpdateAllTagListFlag]);

  useEffect(() => {
    setFetchDetailLoading(true);
    dispatch.datasetDetail.fetchDatasetDetail(currentId).then(() => {
      setFetchDetailLoading(false);
    });
  }, [currentId, dispatch.datasetDetail]);

  useEffect(() => {
    const params = {
      id: currentId,
      pagination: {
        pageNumber: selector.dataQualityTablePagination.pageNumber,
        pageSize: selector.dataQualityTablePagination.pageSize,
      },
    };
    dispatch.datasetDetail.fetchDataQualities(params);
  }, [
    currentId,
    dispatch.datasetDetail,
    selector.dataQualityTablePagination.pageNumber,
    selector.dataQualityTablePagination.pageSize,
    forceReFetchDataQualityFlag,
  ]);

  const handleClickPull = useCallback(() => {
    dispatch.datasetDetail.pullDataset(currentId);
  }, [currentId, dispatch.datasetDetail]);

  const handleChangeDescription = useCallback(
    value => {
      setUpdateLoading(true);
      dispatch.datasetDetail
        .updateDataset({
          id: currentId,
          updateParams: { description: value },
        })
        .then(() => {
          setUpdateLoading(false);
        });
    },
    [currentId, dispatch.datasetDetail],
  );

  const handleChangeOwners = useCallback(
    (value: string[]) => {
      setUpdateLoading(true);
      dispatch.datasetDetail
        .updateDataset({
          id: currentId,
          updateParams: { owners: value },
        })
        .then(() => {
          setUpdateLoading(false);
        });
    },
    [currentId, dispatch.datasetDetail],
  );

  const handleChangeTags = useCallback(
    (value: string[]) => {
      setUpdateLoading(true);
      dispatch.datasetDetail
        .updateDataset({
          id: currentId,
          updateParams: { tags: value },
        })
        .then(() => {
          setForceUpdateAllTagListFlag(v => v + 1);
          setUpdateLoading(false);
        });
    },
    [currentId, dispatch.datasetDetail],
  );

  const handleFinishUpdate = useCallback(() => {
    setFetchColumnsLoading(true);
    const params = {
      id: currentId,
      keyword: debounceColumnKeyword,
      pagination: selector.columnsPagination,
    };
    dispatch.datasetDetail.fetchDatasetColumns(params).then(() => {
      setFetchColumnsLoading(false);
    });
  }, [
    currentId,
    debounceColumnKeyword,
    dispatch.datasetDetail,
    selector.columnsPagination,
  ]);

  const handleChangePagination = useCallback(
    (pageNumber: number, pageSize?: number) => {
      dispatch.datasetDetail.updatePagination({
        pageNumber,
        pageSize: pageSize || 25,
      });
    },
    [dispatch.datasetDetail],
  );
  const handleChangePageSize = useCallback(
    (_pageNumber: number, pageSize: number) => {
      dispatch.datasetDetail.updatePagination({
        pageNumber: 1,
        pageSize: pageSize || 25,
      });
    },
    [dispatch.datasetDetail],
  );

  const handleChangeColumnKeyword = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      dispatch.datasetDetail.updateState({
        key: 'columnsKeyword',
        value: e.target.value,
      });
    },
    [dispatch.datasetDetail],
  );

  const handleClickAddDataQuality = useCallback(() => {
    setCurrentDataQualityId(null);
    setAddDataQualityModalVisible(true);
  }, []);
  const handleCloseAddDataQuality = useCallback(() => {
    setCurrentDataQualityId(null);
    setAddDataQualityModalVisible(false);
  }, []);

  const handleClickEditDataQuality = useCallback((dqId: string) => {
    setCurrentDataQualityId(dqId);
    setAddDataQualityModalVisible(true);
  }, []);

  const defaultRelatedTable = useMemo(
    () => ({
      id: selector.id || '',
      name: selector.name || '',
      datasource: selector.datasource || '',
    }),
    [selector.datasource, selector.id, selector.name],
  );

  const handleConfirmAddDataQuality = useCallback(() => {
    setForceReFetchDataQualityFlag(i => i + 1);
  }, []);

  const handleConfirmDeleteDataQuality = useCallback(
    qualityId => {
      dispatch.datasetDetail.deleteDataQuality({ id: qualityId }).then(resp => {
        if (resp) {
          const newDataQualities = selector.dataQualities?.filter(
            i => i.id !== resp.id,
          );
          dispatch.datasetDetail.updateState({
            key: 'dataQualities',
            value: newDataQualities,
          });
        }
      });
    },
    [dispatch.datasetDetail, selector.dataQualities],
  );

  const handleChangeColumnDescription = useCallback(
    (v: string, id: string) => {
      const diss = message.loading(t('common.loading'), 0);
      dispatch.datasetDetail.updateColumn({ id, description: v }).then(resp => {
        if (resp) {
          handleFinishUpdate();
        }
        diss();
      });
    },
    [dispatch.datasetDetail, handleFinishUpdate, t],
  );

  const columns = useMemo(
    () => [
      {
        title: t('dataDetail.column.name'),
        dataIndex: 'name',
        key: 'name',
        width: 120,
      },
      {
        title: t('dataDetail.column.type'),
        dataIndex: 'type',
        key: 'type',
        width: 120,
      },
      {
        title: t('dataDetail.column.notNullCount'),
        dataIndex: 'not_null_count',
        key: 'not_null_count',
        width: 120,
      },
      {
        title: t('dataDetail.column.notNullPer'),
        dataIndex: 'not_null_percentage',
        key: 'not_null_percentage',
        render: (per: number) => numeral(per).format('0.00%'),
        width: 120,
      },
      {
        title: t('dataDetail.column.description'),
        dataIndex: 'description',
        key: 'description',
        render: (desc: string, record: Column) => (
          <ColumnDescInput
            className={styles.columnDescInput}
            value={desc}
            onChange={v => {
              handleChangeColumnDescription(v, record.id);
            }}
          />
        ),
      },
      {
        title: t('dataDetail.column.updateTime'),
        dataIndex: 'highWatermark',
        key: 'highWatermark',
        render: (waterMark: Watermark) => watermarkFormatter(waterMark?.time),
        width: 200,
      },
    ],
    [handleChangeColumnDescription, t],
  );

  const pagination: TablePaginationConfig = useMemo(
    () => ({
      size: 'small',
      total: selector.columnsPagination.totalCount,
      showTotal: (total: number) => t('dataDetail.column.total', { total }),
      showSizeChanger: true,
      showQuickJumper: true,
      onChange: handleChangePagination,
      onShowSizeChange: handleChangePageSize,
      pageSize: selector.columnsPagination.pageSize,
      pageSizeOptions: ['25', '50', '100', '200'],
    }),
    [
      handleChangePageSize,
      handleChangePagination,
      selector.columnsPagination.pageSize,
      selector.columnsPagination.totalCount,
      t,
    ],
  );

  return (
    <div className={styles.page}>
      <BackButton defaultUrl="/data-discovery/dataset" />

      <Spin spinning={fetchDetailLoading}>
        <Card className={c(styles.pageContent, {
          [styles.deleted]: deleted,
        })}>
          <div className={styles.titleRow}>
            <span className={styles.titleAndWatermark}>
              <span className={styles.title}>
                {selector.name}
                {deleted ? ` (${t('dataDetail.deleted')})` : ''}
              </span>
            </span>

            <div className={styles.headingButtonGroup}>
              <Button
                size="large"
                type="primary"
                onClick={handleClickPull}
                disabled={latestPullProcessIsLoading || isPullingStatus(latestPullProcess)}
                loading={isPullingStatus(latestPullProcess)}
              >
                {isPullingStatus(latestPullProcess) ?
                  t('dataDetail.baseItem.title.pulling', {
                    status: latestPullProcess?.latestMCETaskRun?.status || 'UNKNOWN'
                  }) :
                  t('dataDetail.button.pull')}
              </Button>
            </div>
          </div>

          <div className={styles.detailInfoArea}>
            <Spin spinning={updateLoading}>
              <div className={styles.baseInfoRow}>
                <div className={styles.infoBlock}>
                  <div className={styles.baseItemTitle}>
                    {t('dataDetail.baseItem.title.database')}
                  </div>
                  <div className={styles.baseContent}>{selector.database}</div>
                </div>

                <div className={styles.infoBlock}>
                  <div className={styles.baseItemTitle}>
                    {t('dataDetail.baseItem.title.dbType')}
                  </div>
                  <div className={styles.baseContent}>{selector.type}</div>
                </div>

                <div className={styles.infoBlock}>
                  <div className={styles.baseItemTitle}>
                    {t('dataDetail.baseItem.title.datasource')}
                  </div>
                  <div className={styles.baseContent}>
                    {selector.datasource}
                  </div>
                </div>

                <div
                  className={styles.infoBlock}
                  style={{ marginLeft: 'auto' }}
                >
                  <div
                    className={styles.baseContent}
                    style={{ marginBottom: 8 }}
                  >
                    {t('dataDetail.baseItem.title.rowCount')}
                  </div>
                  <div className={styles.importantContent}>
                    {numeral(selector.rowCount).format('0,0')}
                  </div>
                </div>
                <div className={styles.infoBlock}>
                  <div
                    className={styles.baseContent}
                    style={{ marginBottom: 8 }}
                  >
                    {t('dataDetail.baseItem.title.lastUpdate')}
                  </div>
                  <div className={styles.importantContent}>
                    {selector.highWatermark?.time && (
                      <span className={styles.watermark}>
                        {watermarkFormatter(selector.highWatermark?.time)}
                      </span>
                    )}
                  </div>
                </div>
              </div>
              <Divider className={styles.divider} />

              <div className={styles.inputRow}>
                {selector.glossaries && selector.glossaries.length > 0 && (
                  <div className={styles.glossaryRow}>
                    <div className={styles.baseItemTitle}>
                      {t('dataDetail.baseItem.title.glossary')}
                    </div>

                    <div className={styles.glossaryContent}>
                      {selector.glossaries.map(glossary => (
                        <div key={glossary.id} className={styles.glossaryItem}>
                          <FileTextOutlined style={{ marginRight: 4 }} />
                          <Link
                            to={getBackPath(
                              `/data-discovery/glossary/${glossary.id}`,
                            )}
                          >
                            <div className={styles.glossaryName}>
                              {glossary.name}
                            </div>
                          </Link>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                <div className={styles.shortInputRow}>
                  <div className={styles.infoBlock} style={{ minWidth: 380 }}>
                    <div className={styles.baseItemTitle}>
                      {t('dataDetail.baseItem.title.tags')}
                    </div>
                    <div className={styles.baseContent}>
                      <Select
                        mode="tags"
                        style={{ width: '100%' }}
                        placeholder={t('dataDiscovery.pleaseSelect')}
                        value={selector.tags || []}
                        onChange={handleChangeTags}
                      >
                        {allTagList.map(tagItem => (
                          <Option key={tagItem} value={tagItem}>
                            {tagItem}
                          </Option>
                        ))}
                      </Select>
                    </div>
                  </div>

                  <div className={styles.infoBlock} style={{ minWidth: 380 }}>
                    <div className={styles.baseItemTitle}>
                      {t('dataDetail.baseItem.title.owners')}
                    </div>
                    <div className={styles.baseContent}>
                      <Select
                        mode="multiple"
                        style={{ width: '100%' }}
                        placeholder={t('dataDiscovery.pleaseSelect')}
                        value={selector.owners || []}
                        onChange={handleChangeOwners}
                      >
                        {allOwnerList.map(ownerItem => (
                          <Option key={ownerItem} value={ownerItem}>
                            {ownerItem}
                          </Option>
                        ))}
                      </Select>
                    </div>
                  </div>
                </div>

                <div>
                  <DescriptionInput
                    value={selector.description || ''}
                    onChange={handleChangeDescription}
                  />
                </div>
              </div>

              <Divider className={styles.divider} />

              <div className={styles.columnsArea}>
                <Spin spinning={fetchColumnsLoading}>
                  <div className={styles.columnsTitleRow}>
                    <span className={styles.baseItemTitle}>
                      {t('dataDetail.baseItem.title.clolumns')}
                    </span>

                    <div className={styles.columnSearch}>
                      <Input.Search
                        style={{ width: '100%', maxWidth: 620 }}
                        value={selector.columnsKeyword}
                        onChange={handleChangeColumnKeyword}
                        size="middle"
                      />
                    </div>
                  </div>

                  <Table
                    size="small"
                    columns={columns}
                    pagination={pagination}
                    dataSource={selector.columns}
                    rowKey="id"
                  />
                </Spin>
              </div>

              <Divider className={styles.divider} />
              <div className={styles.columnsArea}>
                <div className={styles.lineageTitleRow}>
                  <span
                    className={styles.baseItemTitle}
                    style={{ marginRight: 8 }}
                  >
                    {t('dataDetail.lineage.title')}
                  </span>
                  {((selector.upstreamLineageTaskList &&
                    selector.upstreamLineageTaskList.length > 0) ||
                    (selector.downstreamLineageTaskList &&
                      selector.downstreamLineageTaskList.length > 0)) && (
                    <Link
                      style={{ textDecoration: 'underLine' }}
                      to={`/data-discovery/dataset/${currentId}/lineage`}
                    >
                      {t('dataDetail.lineage.lineageDetailLink')}
                    </Link>
                  )}
                </div>

                <div className={styles.lineageArea}>
                  <LineageStreamTaskTable
                    datasetId={currentId}
                    direction={LineageDirection.UPSTREAM}
                  />
                  <div className={styles.lineageDivider} />
                  <LineageStreamTaskTable
                    datasetId={currentId}
                    direction={LineageDirection.DOWNSTREAM}
                  />
                </div>
              </div>
              <Divider className={styles.divider} />

              <div className={styles.dataQualityArea}>
                <div className={styles.baseItemTitle}>
                  {t('dataDetail.baseItem.title.dataQuality')}
                  <span
                    className={styles.addButton}
                    onClick={handleClickAddDataQuality}
                  >
                    {t('common.button.add')}
                  </span>
                </div>
                <div className={styles.baseContent}>
                  {selector.dataQualities &&
                    selector.dataQualities.length > 0 && (
                      <DataQualityTable
                        data={selector.dataQualities}
                        onDelete={handleConfirmDeleteDataQuality}
                        onClick={handleClickEditDataQuality}
                      />
                    )}
                </div>
              </div>
            </Spin>
          </div>
        </Card>
        <AddDataQualityModal
          visible={AddDataQualityModalVisible}
          onClose={handleCloseAddDataQuality}
          datasourceType={selector.type}
          relatedTable={defaultRelatedTable}
          onConfirm={handleConfirmAddDataQuality}
          datasetId={currentId}
          dataQualityId={currentDataQualityId}
        />
      </Spin>
    </div>
  );
}
