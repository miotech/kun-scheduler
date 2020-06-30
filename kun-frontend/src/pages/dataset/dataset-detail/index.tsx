import React, { useEffect, useState, useCallback } from 'react';
import { RouteComponentProps } from 'react-router';
import { Link } from 'umi';
import { Spin, Button, message, Select, Pagination, Input } from 'antd';
import Card from '@/components/Card/Card';

import { watermarkFormatter } from '@/utils';

import useI18n from '@/hooks/useI18n';
import useRedux from '@/hooks/useRedux';
import useDebounce from '@/hooks/useDebounce';
import DescriptionInput from './components/DescriptionInput/DescriptionInput';
import ColumnItem from './components/ColumnItem/ColumnItem';

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

  useEffect(() => {
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

  const [forceUpdateAllTagListFlag, setForceUpdateAllTagListFlag] = useState(1);

  useEffect(() => {
    dispatch.dataDiscovery.fetchAllTagList();
  }, [dispatch.dataDiscovery, forceUpdateAllTagListFlag]);

  useEffect(() => {
    setFetchDetailLoading(true);
    dispatch.datasetDetail.fetchDatasetDetail(currentId).then(() => {
      setFetchDetailLoading(false);
    });
  }, [currentId, dispatch.datasetDetail]);

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
    (pageSize: number) => {
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

  return (
    <div className={styles.page}>
      <div className={styles.backButtonRow}>
        <Link to="/data-discovery/dataset">
          {'< '}
          {t('dataDetail.back')}
        </Link>
      </div>
      <Spin spinning={fetchDetailLoading}>
        <Card className={styles.pageContent}>
          <div className={styles.titleRow}>
            <span className={styles.titleAndWatermark}>
              <span className={styles.title}>{selector.name}</span>
              <span className={styles.watermark}>
                {`${watermarkFormatter(
                  selector.low_watermark?.time,
                )} - ${watermarkFormatter(selector.low_watermark?.time)}`}
              </span>
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
                        <Link to={`/flow-and-operator/flow/${flow.flow_id}`}>
                          {flow.flow_name}
                        </Link>
                      ))}
                    </div>
                  </div>
                )}

                <div className={styles.baseItem}>
                  <div className={styles.baseItemTitle}>
                    {t('dataDetail.baseItem.title.schema')}
                  </div>
                  <div className={styles.baseContent}>{selector.schema}</div>
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
                    {t('dataDetail.baseItem.title.database')}
                  </div>
                  <div className={styles.baseContent}>{selector.database}</div>
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
      </Spin>
    </div>
  );
}
