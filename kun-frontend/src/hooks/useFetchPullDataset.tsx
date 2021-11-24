import { useHistory } from 'umi';
import React, { useCallback, useRef } from 'react';
import { notification } from 'antd';
import map from 'lodash/map';
import { get } from '@/utils/requestUtils';
import { METADATA_PREFIX } from '@/constants/api-prefixes';
import { DatasetPullProcessVO } from '@/services/datasetDetail';
import useI18n from './useI18n';

export interface DatasetDtatusFetchResp {
  string: DatasetPullProcessVO;
}
export interface DatasetPullProcessStorageItem extends DatasetPullProcessVO {
  id: string;
  name: string;
}

export default function useFetchPullDataset() {
  const t = useI18n();
  const history = useHistory();
  const interval = useRef<NodeJS.Timeout | null>(null);

  // 拿到所有链接
  const getAllSuccessLink = useCallback(
    (list: string[]) => {
      const localJsonString = localStorage.getItem('datasetPullingList');
      if (localJsonString) {
        const localJson: DatasetPullProcessStorageItem[] = JSON.parse(localJsonString);
        return (
          <div>
            {list.map(i => {
              const currentItem = localJson.find(item => item.id === i);
              if (currentItem) {
                return (
                  <div key={i}>
                    <a
                      href={`/data-discovery/dataset?dsIdList=${i}`}
                      onClick={e => {
                        e.preventDefault();
                        history.push(`/data-discovery/dataset?dsIdList=${i}`);
                      }}
                    >
                      {currentItem?.name}
                    </a>
                  </div>
                );
              }
              return null;
            })}
          </div>
        );
      }
      return undefined;
    },
    [history],
  );

  // 拿到所有失败条目
  const getAllFailedItem = useCallback((list: string[]) => {
    const localJsonString = localStorage.getItem('datasetPullingList');
    if (localJsonString) {
      const localJson: DatasetPullProcessStorageItem[] = JSON.parse(localJsonString);
      return (
        <div>
          {list.map(i => {
            const currentItem = localJson.find(item => item.id === i);
            if (currentItem) {
              return <div key={i}>{currentItem?.name}</div>;
            }
            return null;
          })}
        </div>
      );
    }
    return undefined;
  }, []);

  // 往localstorage里面塞pulling item
  const pushDatasetPullingItem = useCallback((i: DatasetPullProcessStorageItem) => {
    const localJsonString = localStorage.getItem('datasetPullingList');
    let list: DatasetPullProcessStorageItem[] = [];
    if (localJsonString) {
      list = JSON.parse(localJsonString);
    }
    if (!list.find(item => item.id === i.id)) {
      list.push(i);
    }
    localStorage.setItem('datasetPullingList', JSON.stringify(list));
  }, []);

  // 从localstorage里面往外拿pulling item
  const dropDatasetPullingItems = useCallback((ids: string[]) => {
    const localJsonString = localStorage.getItem('datasetPullingList');
    let list: DatasetPullProcessStorageItem[] = [];
    if (localJsonString) {
      list = JSON.parse(localJsonString);
    }
    const newList = list.filter(i => !ids.includes(i.id));
    localStorage.setItem('datasetPullingList', JSON.stringify(newList));
  }, []);

  const fetchAllDatasetPull = useCallback(async () => {
    const localJsonString = localStorage.getItem('datasetPullingList');
    if (localJsonString) {
      const localJson: DatasetPullProcessStorageItem[] = JSON.parse(localJsonString);
      const idList = localJson.map(i => i.id);
      if (idList.length > 0) {
        const resp = await get<DatasetDtatusFetchResp>('/datasource/processes/latest', {
          query: {
            dataSourceIds: idList.join(','),
          },
          prefix: METADATA_PREFIX,
        });
        const needShowSuccessIdList = map(resp, (v, k) => ({ v, k }))
          .filter(i => i.v.latestMCETaskRun?.status === 'SUCCESS')
          .map(i => i.k);
        const needShowFailedIdList = map(resp, (v, k) => ({ v, k }))
          .filter(i => i.v.latestMCETaskRun?.status === 'FAILED')
          .map(i => i.k);

        if (needShowSuccessIdList.length > 0) {
          notification.success({
            message: t('datasetPull.notification.success.title'),
            description: getAllSuccessLink(needShowSuccessIdList),
            duration: 8,
          });
          dropDatasetPullingItems(needShowSuccessIdList);
        }
        if (needShowFailedIdList.length > 0) {
          notification.warning({
            message: t('datasetPull.notification.failed.title'),
            description: getAllFailedItem(needShowFailedIdList),
            onClick: () => {
              history.push('/settings/data-sources');
            },
            duration: 8,
          });
          dropDatasetPullingItems(needShowFailedIdList);
        }
      }
    }
  }, [dropDatasetPullingItems, getAllFailedItem, getAllSuccessLink, history, t]);

  // 开启轮询
  const startFetchInterval = useCallback(() => {
    if (!interval.current) {
      interval.current = setInterval(() => {
        fetchAllDatasetPull();
      }, 30000);
    }
  }, [fetchAllDatasetPull]);

  // 关闭轮询
  const stopFetchInterval = useCallback(() => {
    if (interval.current) {
      clearInterval(interval.current);
      interval.current = null;
    }
  }, []);

  return {
    startFetchInterval,
    stopFetchInterval,
    pushDatasetPullingItem,
    dropDatasetPullingItems,
  };
}
