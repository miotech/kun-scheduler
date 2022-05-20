import produce from 'immer';
import { fetchAllTagsService, fetchAllUsersService, searchDatasetsService } from '@/services/dataDiscovery';
import { Pagination } from '@/definitions/common-types';
import { Dataset } from '@/definitions/Dataset.type';

import { RootDispatch } from '../store';

export enum Mode {
  ABSOLUTE = 'absolute',
  QUICK = 'quick',
}

export interface SearchParams {
  searchContent?: string;
  datasource: string;
  database: string;
  schema: string;
  type: string;
  tags: string;
  owners: string;
  pagination: Pagination;
}

export interface SearchParamsObj {
  keyword?: string;
  datasource: string;
  database: string;
  schema: string;
  type: string;
  tags: string;
  owners: string;
}

export interface QueryObj extends SearchParamsObj {
  pageSize: number;
  pageNumber: number;
}

export interface DsFilterItem {
  id: string;
  name: string;
}

export interface DatabaseFilterItem {
  name: string;
}

export interface DataDiscoveryState {
  searchContent: string;
  selectOptions: {
    datasource: [];
    database: [];
    schema: [];
    type: [];
    tags: [];
    owners: [];
  };

  allDbList: DatabaseFilterItem[];
  allOwnerList: string[];
  allTagList: string[];
  allDsList: DsFilterItem[];

  pagination: Pagination;

  datasetList: Dataset[];

  dataListFetchLoading: boolean;

  sortKey: string | null;
  sortOrder: 'asc' | 'desc' | null;
}

export const dataDiscovery = {
  state: {
    searchContent: '',
    selectOptions: {
      datasource: [],
      database: [],
      schema: [],
      type: [],
      tags: [],
      owners: [],
    },
    allDbList: [],
    allOwnerList: [],
    allTagList: [],
    allDsList: [],
    pagination: {
      pageNumber: 1,
      pageSize: 25,
      totalCount: 0,
    },

    datasetList: [],
    dataListFetchLoading: false,

    sortKey: null,
    sortOrder: null,
  } as DataDiscoveryState,

  reducers: {
    updateState: (state: DataDiscoveryState, payload: { key: keyof DataDiscoveryState; value: any }) => ({
      ...state,
      [payload.key]: payload.value,
    }),
    updateOptions: (state: DataDiscoveryState, payload: { key: string; value: any }) => ({
      ...state,
      selectOptions: {
        ...state.selectOptions,
        [payload.key]: payload.value,
      },
    }),
    batchUpdateState: (state: DataDiscoveryState, payload: Partial<DataDiscoveryState>) => ({
      ...state,
      ...payload,
    }),
    updateFilterAndPaginationFromUrl: (state: DataDiscoveryState, payload: Partial<DataDiscoveryState>) => {
      return {
        ...state,
        ...payload,
      };
    },
    updateFilter: (state: DataDiscoveryState, payload: { key: keyof DataDiscoveryState; value: any }) => ({
      ...state,
      [payload.key]: payload.value,
      pagination: {
        ...state.pagination,
        pageNumber: 1,
      },
    }),
    updateDataListFetchLoading: produce((draftState: DataDiscoveryState, payload: boolean) => {
      draftState.dataListFetchLoading = payload;
    }),
  },

  effects: (dispatch: RootDispatch) => {
    let seachDatasetsFlag = 0;
    return {
      async fetchAllOwnerList() {
        try {
          const resp = await fetchAllUsersService();
          if (resp) {
            dispatch.dataDiscovery.updateState({
              key: 'allOwnerList',
              value: resp.users,
            });
          }
        } catch (e) {
          // do nothing
        }
      },
      async fetchAllTagList() {
        try {
          const resp = await fetchAllTagsService();
          if (resp) {
            dispatch.dataDiscovery.updateState({
              key: 'allTagList',
              value: resp.tags,
            });
          }
        } catch (e) {
          // do nothing
        }
      },
      async searchDatasets(payload: SearchParams) {
        const { searchContent, datasource, database, schema, type, tags, owners, pagination } = payload;

        const searchParams: SearchParamsObj = {
          keyword: searchContent,
          datasource,
          database,
          schema,
          type,
          tags,
          owners,
        };
        seachDatasetsFlag += 1;
        const currentSeachDatasetsFlag = seachDatasetsFlag;
        try {
          dispatch.dataDiscovery.updateDataListFetchLoading(true);
          const resp = await searchDatasetsService(searchParams, pagination);
          if (currentSeachDatasetsFlag === seachDatasetsFlag) {
            dispatch.dataDiscovery.updateDataListFetchLoading(false);
            if (resp) {
              const { datasets, pageSize, pageNumber, totalCount } = resp;
              dispatch.dataDiscovery.batchUpdateState({
                datasetList: datasets as Dataset[],
                pagination: {
                  pageSize,
                  pageNumber,
                  totalCount,
                },
              });
            }
          }
        } catch (e) {
          // do nothing
        }
      },
    };
  },
};
