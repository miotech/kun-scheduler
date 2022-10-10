import {
  searchDataBasesService,
  pullDatasetsFromDatabaseService,
  deleteDatabaseService,
  DataSourcePullProcessVO,
  fetchLatestPullProcessesOfDataSources,
} from '@/services/dataSettings';
import { Pagination } from '@/definitions/common-types';
import { DataSourceInfo } from '@/definitions/DataSource.type';
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

export type DatabaseInfomation = {
  [k: string]: {
    [j: string]: string;
  };
};

export interface DataSettingsState {
  searchLoading: boolean;
  searchContent: string;
  pagination: Pagination;
  dataSourceList: DataSourceInfo[];
  currentDatabase: DataSourceInfo | null;
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
                dataSourceList: datasources as DataSourceInfo[],
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
