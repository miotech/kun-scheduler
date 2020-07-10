import produce from 'immer';
import moment from 'moment';
import {
  fetchAllTagsService,
  fetchAllUsersService,
  searchDatasetsService,
  searchAllDbService,
} from '@/services/dataDiscovery';
import { Pagination, DbType } from './index';
import { RootDispatch } from '../store';

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
  dbTypeList?: string[];
  dsIdList?: string[];
  watermarkMode?: Mode;
  watermarkAbsoluteValue?: DataRange;
  watermarkQuickeValue?: Quick;
  pagination: Pagination;
}

export interface SearchParamsObj {
  searchContent?: string;
  watermarkStart?: number;
  watermarkEnd?: number;
  ownerList?: string[];
  tagList?: string[];
  dbTypeList?: string[];
  dsIdList?: string[];
}

export interface dbFilterItem {
  id: string;
  name: string;
}

export interface DataDiscoveryState {
  searchContent: string;
  watermarkMode: Mode;
  watermarkAbsoluteValue?: DataRange;
  watermarkQuickeValue?: Quick;

  ownerList?: string[];
  tagList?: string[];
  dbTypeList?: DbType[];
  dsIdList?: string[];

  allOwnerList: string[];
  allTagList: string[];
  allDbList: dbFilterItem[];

  pagination: Pagination;

  datasetList: Dataset[];

  dataListFetchLoading: boolean;
}

export const dataDiscovery = {
  state: {
    searchContent: '',

    watermarkMode: Mode.ABSOLUTE,
    watermarkAbsoluteValue: undefined,
    watermarkQuickeValue: undefined,

    ownerList: undefined,
    tagList: undefined,
    dbTypeList: undefined,
    dsIdList: undefined,

    allOwnerList: [],
    allTagList: [],
    allDbList: [],

    pagination: {
      pageNumber: 1,
      pageSize: 25,
      totalCount: 0,
    },

    datasetList: [],
    dataListFetchLoading: false,
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
        const resp = await fetchAllUsersService();
        if (resp) {
          dispatch.dataDiscovery.updateState({
            key: 'allOwnerList',
            value: resp.users,
          });
        }
      },
      async fetchAllTagList() {
        const resp = await fetchAllTagsService();
        if (resp) {
          dispatch.dataDiscovery.updateState({
            key: 'allTagList',
            value: resp.tags,
          });
        }
      },

      async searchDatasets(payload: SearchParams) {
        const {
          searchContent,
          ownerList,
          tagList,
          dbTypeList,
          dsIdList,
          watermarkMode,
          watermarkAbsoluteValue,
          watermarkQuickeValue,
          pagination,
        } = payload;
        let watermarkStart: number | undefined;
        let watermarkEnd: number | undefined;
        if (watermarkMode === Mode.ABSOLUTE) {
          if (watermarkAbsoluteValue?.startTime) {
            watermarkStart = watermarkAbsoluteValue.startTime;
          }
          if (watermarkAbsoluteValue?.endTime) {
            watermarkEnd = watermarkAbsoluteValue.endTime;
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
          dbTypeList,
          dsIdList,
        };
        seachDatasetsFlag += 1;
        const currentSeachDatasetsFlag = seachDatasetsFlag;
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
      },
      async fetchAllDb(payload: string) {
        const resp = await searchAllDbService(payload);
        if (resp) {
          dispatch.dataDiscovery.updateState({
            key: 'allDbList',
            value: resp.databases,
          });
        }
      },
    };
  },
};
