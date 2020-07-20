import React, { useEffect, useState, useCallback, useMemo } from 'react';
import { RouteComponentProps } from 'react-router';
import { Link } from 'umi';
import { CloseOutlined } from '@ant-design/icons';
import {
  Spin,
  Button,
  message,
  Select,
  Pagination,
  Input,
  Popconfirm,
} from 'antd';
import Card from '@/components/Card/Card';

import { watermarkFormatter } from '@/utils/glossaryUtiles';

import useI18n from '@/hooks/useI18n';
import useRedux from '@/hooks/useRedux';
import useDebounce from '@/hooks/useDebounce';
import BackButton from '@/components/BackButton/BackButton';

import DescriptionInput from './components/DescriptionInput/DescriptionInput';
import ColumnItem from './components/ColumnItem/ColumnItem';
import AddDataQualityModal from './components/AddDataQualityModal/AddDataQualityModal';

import styles from './index.less';

interface MatchParams {
  datasetId: string;
}

interface Props extends RouteComponentProps<MatchParams> {}

const { Option } = Select;

export default function DatasetDetail({ match }: Props) {
  const t = useI18n();

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
  const [forceReFetchDetailFlag, setForceReFetchDetailFlag] = useState(1);

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
  }, [currentId, dispatch.datasetDetail, forceReFetchDetailFlag]);

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
    setForceReFetchDetailFlag(i => i + 1);
  }, []);

  const handleConfirmDeleteDataQuality = useCallback(
    qualityId => {
      dispatch.datasetDetail
        .deleteDataQuality({ id: qualityId, datasetId: currentId })
        .then(resp => {
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
    [currentId, dispatch.datasetDetail, selector.dataQualities],
  );

  return (
    <div className={styles.page}>
      <BackButton defaultUrl="/data-discovery/dataset" />

      <Spin spinning={fetchDetailLoading}>
        <Card className={styles.pageContent}>
          <div className={styles.titleRow}>
            <span className={styles.titleAndWatermark}>
              <span className={styles.title}>{selector.name}</span>
              {selector.low_watermark?.time &&
                selector.high_watermark?.time && (
                  <span className={styles.watermark}>
                    {`${watermarkFormatter(
                      selector.low_watermark?.time,
                    )} - ${watermarkFormatter(selector.high_watermark?.time)}`}
                  </span>
                )}
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
            <div className={styles.baseInfoArea}>
              <Spin spinning={updateLoading}>
                {/* <div className={styles.baseItem}>
                <div className={styles.baseItemTitle}>
                  {t('dataDetail.baseItem.title.lineage')}
                </div>
                <div className={styles.baseContent}>

                </div>
              </div> */}

                {(selector.flows?.length ?? 0) > 0 && (
                  <div className={styles.baseItem}>
                    <div className={styles.baseItemTitle}>
                      {t('dataDetail.baseItem.title.task')}
                    </div>
                    <div className={styles.baseContent}>
                      {selector.flows?.map(flow => (
                        <Link
                          key={flow.flow_id}
                          to={`/flow-and-operator/flow/${flow.flow_id}`}
                        >
                          {flow.flow_name}
                        </Link>
                      ))}
                    </div>
                  </div>
                )}

                <div className={styles.baseItem}>
                  <div className={styles.baseItemTitle}>
                    {t('dataDetail.baseItem.title.database')}
                  </div>
                  <div className={styles.baseContent}>{selector.database}</div>
                </div>

                <div className={styles.baseItem}>
                  <div className={styles.baseItemTitle}>
                    {t('dataDetail.baseItem.title.dbType')}
                  </div>
                  <div className={styles.baseContent}>{selector.type}</div>
                </div>

                <div className={styles.baseItem}>
                  <DescriptionInput
                    value={selector.description || ''}
                    onChange={handleChangeDescription}
                  />
                </div>

                <div className={styles.baseItem}>
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
                    {selector.dataQualities?.map(item => (
                      <div key={item.id}>
                        <span
                          className={styles.dataQualityItem}
                          onClick={() => handleClickEditDataQuality(item.id)}
                        >
                          {item.name}
                        </span>
                        <Popconfirm
                          title={t('dataDetail.dataquality.delete.title')}
                          onConfirm={() =>
                            handleConfirmDeleteDataQuality(item.id)
                          }
                          okText={t('common.button.confirm')}
                          cancelText={t('common.button.cancel')}
                        >
                          <CloseOutlined style={{ marginLeft: 4 }} />
                        </Popconfirm>
                      </div>
                    ))}
                  </div>
                </div>

                <div className={styles.baseItem}>
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

                <div className={styles.baseItem}>
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

                <div className={styles.baseItem}>
                  <div className={styles.baseItemTitle}>
                    {t('dataDetail.baseItem.title.rowCount')}
                  </div>
                  <div className={styles.baseContent}>{selector.row_count}</div>
                </div>

                <div className={styles.baseItem}>
                  <div className={styles.baseItemTitle}>
                    {t('dataDetail.baseItem.title.datasource')}
                  </div>
                  <div className={styles.baseContent}>
                    {selector.datasource}
                  </div>
                </div>
              </Spin>
            </div>

            <div className={styles.columnsArea}>
              <div className={styles.columnsAreaInner}>
                <div className={styles.columnSearch}>
                  <Input.Search
                    style={{ width: '70%', maxWidth: 620 }}
                    value={selector.columnsKeyword}
                    onChange={handleChangeColumnKeyword}
                    size="large"
                  />
                </div>
                <Spin spinning={fetchColumnsLoading}>
                  {selector.columns?.map(column => (
                    <ColumnItem
                      key={column.id}
                      column={column}
                      onFinishUpdate={handleFinishUpdate}
                    />
                  ))}
                  <div className={styles.pagination}>
                    <Pagination
                      size="small"
                      total={selector.columnsPagination.totalCount}
                      showTotal={total =>
                        t('dataDetail.column.total', { total })
                      }
                      showSizeChanger
                      showQuickJumper
                      onChange={handleChangePagination}
                      onShowSizeChange={handleChangePageSize}
                      pageSize={selector.columnsPagination.pageSize}
                      pageSizeOptions={['25', '50', '100', '200']}
                    />
                  </div>
                </Spin>
              </div>
            </div>
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
