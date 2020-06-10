// import produce from 'immer';
import moment from 'moment';
import {
  fetchAllTagsService,
  fetchAllUsersService,
  searchDatasetsService,
} from '@/services/dataDiscovery';
import { Pagination, DbType } from './index';
import { RootDispatch, RootState } from '../store';

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

export interface Dataset {
  id: string;
  name: string;
  schema: string;
  database_name: string;
  description: string;
  type: string;
  tags: string[];
  owners: string[];
  high_watermark: Watermark;
}

export interface SearchParamsObj {
  searchContent?: string;
  watermarkStart?: number;
  watermarkEnd?: number;
  ownerList?: string[];
  tagList?: string[];
  dbTypeList?: DbType[];
}

export interface DataDiscoveryState {
  searchContent: string;
  wartermarkMode: Mode;
  wartermarkAbsoluteValue?: DataRange;
  wartermarkQuickeValue?: Quick;

  ownerList?: string[];
  tagList?: string[];
  dbTypeList?: DbType[];

  allOwnerList: string[];
  allTagList: string[];

  pagination: Pagination;

  datasetList: Dataset[];
}

export const dataDiscovery = {
  state: {
    searchContent: '',

    wartermarkMode: Mode.ABSOLUTE,
    wartermarkAbsoluteValue: undefined,
    wartermarkQuickeValue: undefined,

    ownerList: undefined,
    tagList: undefined,
    dbTypeList: undefined,

    allOwnerList: [],
    allTagList: [],

    pagination: {
      pageNumber: 1,
      pageSize: 25,
      totalCount: 0,
    },

    datasetList: [],
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
  },

  effects: (dispatch: RootDispatch) => ({
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

    async searchDatasets(_payload: any, rootState: RootState) {
      const {
        searchContent,
        ownerList,
        tagList,
        dbTypeList,
        wartermarkMode,
        wartermarkAbsoluteValue,
        wartermarkQuickeValue,
        pagination,
      } = rootState.dataDiscovery;
      let watermarkStart: number | undefined;
      let watermarkEnd: number | undefined;
      if (wartermarkMode === Mode.ABSOLUTE) {
        if (wartermarkAbsoluteValue?.startTime) {
          watermarkStart = wartermarkAbsoluteValue.startTime;
        }
        if (wartermarkAbsoluteValue?.endTime) {
          watermarkEnd = wartermarkAbsoluteValue.endTime;
        }
      }

      if (wartermarkMode === Mode.QUICK) {
        switch (wartermarkQuickeValue) {
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
      };

      const resp = await searchDatasetsService(searchParams, pagination);
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
    },
  }),
};
