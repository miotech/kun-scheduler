import {
  searchDataBasesService,
  addDatabaseService,
  updateDatabaseService,
  pullDatasetsFromDatabaseService,
  deleteDatabaseService,
  fetchDatabaseTypesService,
  DataSourcePullProcessVO,
  fetchLatestPullProcessesOfDataSources,
} from '@/services/dataSettings';
import { Pagination } from '@/definitions/common-types';
import { DbType } from '@/definitions/Database.type';
import { Watermark } from '@/definitions/Dataset.type';
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
  highWatermark: Watermark;
}

export interface DataSettingsState {
  searchLoading: boolean;
  searchContent: string;
  pagination: Pagination;
  dataSourceList: DataSource[];
  currentDatabase: DataSource | null;
  databaseTypeFieldMapList: DatabaseTypeList;
  fetchDatabaseTypeLoading: boolean;
  pullProcesses: Record<string, DataSourcePullProcessVO>;
  pullProcessesIsLoading: boolean;
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
    pullProcesses: {},
    pullProcessesIsLoading: false,
  } as DataSettingsState,
  reducers: {
    updateFilter: (state: DataSettingsState, payload: { key: keyof DataSettingsState; value: any }) => ({
      ...state,
      [payload.key]: payload.value,
      pagination: {
        ...state.pagination,
        pageNumber: 1,
      },
    }),
    updateState: (state: DataSettingsState, payload: { key: keyof DataSettingsState; value: any }) => ({
      ...state,
      [payload.key]: payload.value,
    }),
    batchUpdateState: (state: DataSettingsState, payload: Partial<DataSettingsState>) => ({
      ...state,
      ...payload,
    }),
    setPullProcesses: (state: DataSettingsState, payload: Record<string, DataSourcePullProcessVO>) => {
      return {
        ...state,
        pullProcesses: payload,
      };
    },
    setPullProcessesIsLoading: (state: DataSettingsState, payload: boolean) => {
      return {
        ...state,
        pullProcessesIsLoading: payload,
      };
    },
  },
  effects: (dispatch: RootDispatch) => {
    let searchDataBasesFlag = 0;
    return {
      async fetchDatabaseTypeList() {
        try {
          dispatch.dataSettings.updateState({
            key: 'fetchDatabaseTypeLoading',
            value: true,
          });
          const resp = await fetchDatabaseTypesService();
          if (resp) {
            dispatch.dataSettings.updateState({
              key: 'databaseTypeFieldMapList',
              value: resp,
            });
          }
        } catch (e) {
          // do nothing
        } finally {
          dispatch.dataSettings.updateState({
            key: 'fetchDatabaseTypeLoading',
            value: false,
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
        try {
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
              dispatch.dataSettings.fetchLatestPullProcesses((datasources || []).map(ds => ds.id));
            }
          }
        } catch (e) {
          // do nothing
        }
      },

      async addDatabase(newDatabase: DatasourceInfo) {
        try {
          const resp = await addDatabaseService(newDatabase);
          if (resp) {
            return resp;
          }
        } catch (e) {
          // do nothing
        }
        return null;
      },

      async updateDatabase(newDatabase: UpdateDatasourceInfo) {
        try {
          const resp = await updateDatabaseService(newDatabase);
          if (resp) {
            return resp;
          }
        } catch (e) {
          // do nothing
        }
        return null;
      },

      async pullDatasetsFromDatabase(id: string) {
        try {
          const resp = await pullDatasetsFromDatabaseService(id);
          if (resp) {
            return resp;
          }
        } catch (e) {
          // do nothing
        }
        return null;
      },

      async deleteDatabase(id: string) {
        try {
          const resp = await deleteDatabaseService(id);
          if (resp) {
            return resp;
          }
        } catch (e) {
          // do nothing
        }
        return null;
      },

      async fetchLatestPullProcesses(dataSourceIds: string[]) {
        dispatch.dataSettings.setPullProcessesIsLoading(true);
        try {
          const response = await fetchLatestPullProcessesOfDataSources(dataSourceIds);
          if (response) {
            dispatch.dataSettings.setPullProcesses(response);
          }
        } catch (e) {
          // Do nothing
        } finally {
          dispatch.dataSettings.setPullProcessesIsLoading(false);
        }
      },
    };
  },
};
