import {
  searchDataBasesService,
  addDatabaseService,
  updateDatabaseService,
  pullDatasetsFromDatabaseService,
  deleteDatabaseService,
  fetchDatabaseTypesService,
} from '@/services/dataSettings';
import { Watermark } from './dataDiscovery';
import { Pagination, DbType } from './index';
import { RootDispatch, RootState } from '../store';

export enum DatabaseTypeItemFieldItemFormat {
  INPUT = 'INPUT',
  PASSWORD = 'PASSWORD',
  NUMBER_INPUT = 'NUMBER_INPUT',
}

export enum DatabaseField {
  HOST = 'host',
  PORT = 'port',
  USERNAME = 'username',
  PASSWORD = 'password',
  GLUE_ACCESS_KEY = 'glueAccessKey',
  GLUE_SECRET_KEY = 'glueSecretKey',
  GLUE_REGION = 'glueRegion',
  ATHENA_URL = 'athenaUrl',
  ATHENA_USERNAME = 'athenaUsername',
  ATHENA_PASSWORD = 'athenaPassword',
}

export interface DatabaseTypeItemFieldItem {
  key: DatabaseField;
  order: number;
  format: DatabaseTypeItemFieldItemFormat;
  require: boolean;
}

export interface DatabaseTypeItem {
  id: string;
  type: DbType;
  fields: DatabaseTypeItemFieldItem[];
}

export type DatabaseTypeList = DatabaseTypeItem[];

export type DatabaseInfomation = {
  [k in DatabaseField]?: string | number;
};

export interface DatasourceInfo {
  typeId: string | null;
  name: string;
  information: DatabaseInfomation;
  tags: string[];
}

export interface UpdateDatasourceInfo extends DatasourceInfo {
  id: string;
}

export interface DataSource extends UpdateDatasourceInfo {
  create_user: string;
  create_time: number;
  update_user: string;
  update_time: number;
  high_watermark: Watermark;
}

export interface DataSettingsState {
  searchLoading: boolean;
  searchContent: string;
  pagination: Pagination;
  dataSourceList: DataSource[];
  currentDatabase: DataSource | null;
  databaseTypeFieldMapList: DatabaseTypeList;
  fetchDatabaseTypeLoading: boolean;
}

export const dataSettings = {
  state: {
    searchLoading: false,
    searchContent: '',
    pagination: {
      pageNumber: 1,
      pageSize: 25,
      totalCount: 0,
    },
    dataSourceList: [],
    currentDatabase: null,
    fetchDatabaseTypeLoading: false,
    databaseTypeFieldMapList: [],
  } as DataSettingsState,
  reducers: {
    updateFilter: (
      state: DataSettingsState,
      payload: { key: keyof DataSettingsState; value: any },
    ) => ({
      ...state,
      [payload.key]: payload.value,
      pagination: {
        ...state.pagination,
        pageNumber: 1,
      },
    }),
    updateState: (
      state: DataSettingsState,
      payload: { key: keyof DataSettingsState; value: any },
    ) => ({
      ...state,
      [payload.key]: payload.value,
    }),
    batchUpdateState: (
      state: DataSettingsState,
      payload: Partial<DataSettingsState>,
    ) => ({
      ...state,
      ...payload,
    }),
  },
  effects: (dispatch: RootDispatch) => {
    let searchDataBasesFlag = 0;
    return {
      async fetchDatabaseTypeList() {
        dispatch.dataSettings.updateState({
          key: 'fetchDatabaseTypeLoading',
          value: true,
        });
        const resp = await fetchDatabaseTypesService();
        dispatch.dataSettings.updateState({
          key: 'fetchDatabaseTypeLoading',
          value: false,
        });
        if (resp) {
          dispatch.dataSettings.updateState({
            key: 'databaseTypeFieldMapList',
            value: resp,
          });
        }
      },
      async searchDataBases(_payload: any, rootState: RootState) {
        const { searchContent, pagination } = rootState.dataSettings;
        dispatch.dataSettings.updateState({
          key: 'searchLoading',
          value: true,
        });
        searchDataBasesFlag += 1;
        const currentSearchDataBasesFlag = searchDataBasesFlag;
        const resp = await searchDataBasesService(searchContent, pagination);
        if (currentSearchDataBasesFlag === searchDataBasesFlag) {
          dispatch.dataSettings.updateState({
            key: 'searchLoading',
            value: false,
          });
          if (resp) {
            const { datasources, pageNumber, pageSize, totalCount } = resp;
            dispatch.dataSettings.batchUpdateState({
              dataSourceList: datasources as DataSource[],
              pagination: {
                pageSize,
                pageNumber,
                totalCount,
              },
            });
          }
        }
      },

      async addDatabase(newDatabase: DatasourceInfo) {
        const resp = await addDatabaseService(newDatabase);
        if (resp) {
          return resp;
        }
        return null;
      },

      async updateDatabase(newDatabase: UpdateDatasourceInfo) {
        const resp = await updateDatabaseService(newDatabase);
        if (resp) {
          return resp;
        }
        return null;
      },

      async pullDatasetsFromDatabase(id: string) {
        const resp = await pullDatasetsFromDatabaseService(id);
        if (resp) {
          return resp;
        }
        return null;
      },

      async deleteDatabase(id: string) {
        const resp = await deleteDatabaseService(id);
        if (resp) {
          return resp;
        }
        return null;
      },
    };
  },
};
