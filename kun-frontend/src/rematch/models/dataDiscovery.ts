import produce from 'immer';
import moment from 'moment';
import {
  fetchAllTagsService,
  fetchAllUsersService,
  searchDatasetsService,
  searchAllDsService,
  fetchAllDbService,
} from '@/services/dataDiscovery';
import { searchGlossariesService } from '@/services/glossary';
import { Pagination } from '@/definitions/common-types';
import { DbType } from '@/definitions/Database.type';
import { RootDispatch } from '../store';
import { SearchGlossaryItem } from './glossary';

export interface DataRange {
  startTime: number | null;
  endTime: number | null;
}

export enum Quick {
  LAST_30_M = 'LAST_30_M',
  LAST_4_H = 'LAST_4_H',
  LAST_1_D = 'LAST_1_D',
  LAST_1_W = 'LAST_1_W',
  LAST_1_MON = 'LAST_1_MON',
}

export enum Mode {
  ABSOLUTE = 'absolute',
  QUICK = 'quick',
}

export interface Watermark {
  user: string;
  time: number;
}

export interface GlossaryItem {
  id: string;
  name: string;
}

export interface Dataset {
  id: string;
  name: string;
  schema: string;
  description: string;
  type: string;
  datasource: string;
  database: string;
  high_watermark: Watermark;
  owners: string[];
  tags: string[];
  glossaries: GlossaryItem[];
}

export interface SearchParams {
  searchContent?: string;
  ownerList?: string[];
  tagList?: string[];
  dsTypeList?: string[];
  dsIdList?: string[];
  dbList?: string[];
  glossaryIdList?: string[];
  watermarkMode?: Mode;
  watermarkAbsoluteValue?: DataRange;
  watermarkQuickeValue?: Quick;
  pagination: Pagination;

  sortKey: string | null;
  sortOrder: 'asc' | 'desc' | null;
}

export interface SearchParamsObj {
  searchContent?: string;
  watermarkStart?: number;
  watermarkEnd?: number;
  ownerList?: string[];
  tagList?: string[];
  dsTypeList?: string[];
  dsIdList?: string[];
  dbList?: string[];
  glossaryIdList?: string[];

  sortKey: string | null;
  sortOrder: 'asc' | 'desc' | null;
}

export interface QueryObj extends SearchParamsObj {
  pageSize: number;
  pageNumber: number;

  watermarkMode?: Mode;
  watermarkAbsoluteValue?: DataRange;
  watermarkQuickeValue?: Quick;
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
  watermarkMode: Mode;
  watermarkAbsoluteValue?: DataRange;
  watermarkQuickeValue?: Quick;

  ownerList?: string[];
  tagList?: string[];
  dsTypeList?: DbType[];
  dsIdList?: string[];
  dbList?: string[];
  glossaryIdList?: string[];

  allDbList: DatabaseFilterItem[];
  allOwnerList: string[];
  allTagList: string[];
  allDsList: DsFilterItem[];
  allGlossaryList: SearchGlossaryItem[];

  pagination: Pagination;

  datasetList: Dataset[];

  dataListFetchLoading: boolean;

  sortKey: string | null;
  sortOrder: 'asc' | 'desc' | null;
}

export const dataDiscovery = {
  state: {
    searchContent: '',

    watermarkMode: Mode.ABSOLUTE,
    watermarkAbsoluteValue: undefined,
    watermarkQuickeValue: undefined,

    ownerList: undefined,
    tagList: undefined,
    dsTypeList: undefined,
    dsIdList: undefined,
    dbList: undefined,

    allDbList: [],
    allOwnerList: [],
    allTagList: [],
    allDsList: [],
    allGlossaryList: [],

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
    updateState: (
      state: DataDiscoveryState,
      payload: { key: keyof DataDiscoveryState; value: any },
    ) => ({
      ...state,
      [payload.key]: payload.value,
    }),
    batchUpdateState: (
      state: DataDiscoveryState,
      payload: Partial<DataDiscoveryState>,
    ) => ({
      ...state,
      ...payload,
    }),
    updateFilterAndPaginationFromUrl: (
      state: DataDiscoveryState,
      payload: Partial<DataDiscoveryState>,
    ) => {
      return {
        ...state,
        ...payload,
      };
    },
    updateFilter: (
      state: DataDiscoveryState,
      payload: { key: keyof DataDiscoveryState; value: any },
    ) => ({
      ...state,
      [payload.key]: payload.value,
      pagination: {
        ...state.pagination,
        pageNumber: 1,
      },
    }),
    updateDataListFetchLoading: produce(
      (draftState: DataDiscoveryState, payload: boolean) => {
        draftState.dataListFetchLoading = payload;
      },
    ),
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
        const {
          searchContent,
          ownerList,
          tagList,
          dsTypeList,
          dsIdList,
          dbList,
          glossaryIdList,
          sortKey,
          sortOrder,
          watermarkMode,
          watermarkAbsoluteValue,
          watermarkQuickeValue,
          pagination,
        } = payload;
        let watermarkStart: number | undefined;
        let watermarkEnd: number | undefined;
        if (watermarkMode === Mode.ABSOLUTE) {
          if (
            watermarkAbsoluteValue?.startTime &&
            Number(watermarkAbsoluteValue.startTime)
          ) {
            watermarkStart = watermarkAbsoluteValue.startTime || undefined;
          }
          if (
            watermarkAbsoluteValue?.endTime &&
            Number(watermarkAbsoluteValue.endTime)
          ) {
            watermarkEnd = watermarkAbsoluteValue.endTime || undefined;
          }
        }

        if (watermarkMode === Mode.QUICK) {
          switch (watermarkQuickeValue) {
            case Quick.LAST_30_M:
              watermarkStart = moment()
                .subtract(30, 'minutes')
                .valueOf();
              break;

            case Quick.LAST_4_H:
              watermarkStart = moment()
                .subtract(4, 'hours')
                .valueOf();
              break;

            case Quick.LAST_1_D:
              watermarkStart = moment()
                .subtract(1, 'day')
                .valueOf();
              break;

            case Quick.LAST_1_W:
              watermarkStart = moment()
                .subtract(1, 'week')
                .valueOf();
              break;

            case Quick.LAST_1_MON:
              watermarkStart = moment()
                .subtract(1, 'month')
                .valueOf();
              break;

            default:
              break;
          }
          watermarkEnd = moment().valueOf();
        }

        const searchParams: SearchParamsObj = {
          searchContent,
          watermarkStart,
          watermarkEnd,
          ownerList,
          tagList,
          dsTypeList,
          dsIdList,
          dbList,
          glossaryIdList,
          sortOrder,
          sortKey,
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
      async fetchAllDs(payload: string) {
        try {
          const resp = await searchAllDsService(payload);
          if (resp) {
            dispatch.dataDiscovery.updateState({
              key: 'allDsList',
              value: resp.datasources,
            });
          }
        } catch (e) {
          // do nothing
        }
      },
      async fetchAllDb() {
        try {
          const resp = await fetchAllDbService();
          if (resp) {
            dispatch.dataDiscovery.updateState({
              key: 'allDbList',
              value: resp,
            });
          }
        } catch (e) {
          // do nothing
        }
      },
      async fetchAllGlossary() {
        try {
          const resp = await searchGlossariesService('', 1000000);
          if (resp) {
            dispatch.dataDiscovery.updateState({
              key: 'allGlossaryList',
              value: resp.glossaries,
            });
          }
        } catch (e) {
          // do nothing
        }
      },
    };
  },
};
