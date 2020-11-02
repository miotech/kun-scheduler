import React, { useEffect, useState, useCallback, useMemo } from 'react';
import { Link } from 'umi';
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

export default function DatasetDetail({ match }: Props) {
  const t = useI18n();
  const { getBackPath } = useBackPath();

  const { selector, dispatch } = useRedux(state => state.datasetDetail);
  const {
    selector: { allOwnerList, allTagList },
  } = useRedux(state => ({
    allOwnerList: state.dataDiscovery.allOwnerList,
    allTagList: state.dataDiscovery.allTagList,
  }));

  const debounceColumnKeyword = useDebounce(selector.columnsKeyword, 500);

  const currentId = match.params.datasetId;

  const [fetchDetailLoading, setFetchDetailLoading] = useState(false);
  const [fetchColumnsLoading, setFetchColumnsLoading] = useState(false);
  const [updateLoading, setUpdateLoading] = useState(false);

  const [forceReFetchInfoFlag, setForceReFetchInfoFlag] = useState(1);
  const [forceUpdateAllTagListFlag, setForceUpdateAllTagListFlag] = useState(1);
  const [
    forceReFetchDataQualityFlag,
    setForceReFetchDataQualityFlag,
  ] = useState(1);

  const [AddDataQualityModalVisible, setAddDataQualityModalVisible] = useState(
    false,
  );

  // 编辑 dataquality 用
  const [currentDataQualityId, setCurrentDataQualityId] = useState<
    string | null
  >(null);

  useEffect(() => {
    // 如果更改了搜索关键词, 那么强制切换页码数为1
    dispatch.datasetDetail.updatePagination({ pageNumber: 1 });
  }, [dispatch.datasetDetail, debounceColumnKeyword, forceReFetchInfoFlag]);

  useEffect(() => {
    setFetchColumnsLoading(true);
    const params = {
      id: currentId,
      keyword: debounceColumnKeyword,
      pagination: {
        pageNumber: selector.columnsPagination.pageNumber,
        pageSize: selector.columnsPagination.pageSize,
      },
    };
    dispatch.datasetDetail.fetchDatasetColumns(params).then(() => {
      setFetchColumnsLoading(false);
    });
  }, [
    currentId,
    debounceColumnKeyword,
    dispatch.datasetDetail,
    selector.columnsPagination.pageNumber,
    selector.columnsPagination.pageSize,
    forceReFetchInfoFlag,
  ]);

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
    const diss = message.loading(t('dataDetail.button.pullLoading'), 0);
    dispatch.datasetDetail.pullDataset(currentId).then(resp => {
      diss();
      if (resp) {
        setForceReFetchInfoFlag(v => v + 1);
      }
    });
  }, [currentId, dispatch.datasetDetail, t]);

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
        <Card className={styles.pageContent}>
          <div className={styles.titleRow}>
            <span className={styles.titleAndWatermark}>
              <span className={styles.title}>{selector.name}</span>
            </span>

            <Button
              size="large"
              className={styles.pullButton}
              onClick={handleClickPull}
            >
              {t('dataDetail.button.pull')}
            </Button>
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
                    {selector.rowCount}
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
                    <span className={styles.columnsTitle}>
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
