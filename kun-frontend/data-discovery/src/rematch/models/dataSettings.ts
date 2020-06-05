import {
  searchDataBasesService,
  addDatabaseService,
  updateDatabaseService,
  pullDatasetsFromDatabaseService,
  deleteDatabaseService,
} from '@/services/dataSettings';
import { Watermark } from './dataDiscovery';
import { Pagination, DbType } from './index';
import { RootDispatch, RootState } from '../store';

export interface DatabaseInfo {
  type: DbType | null;
  name: string;
  ip: string;
  username: string;
  password: string;
  tags: string[];
}

export interface UpdateDatabaseInfo extends DatabaseInfo {
  id: string;
}

export interface DataBase extends UpdateDatabaseInfo {
  create_user: string;
  create_time: number;
  update_user: string;
  update_time: number;
  high_watermark: Watermark;
}

export interface DataSettingsState {
  searchContent: string;
  pagination: Pagination;
  dataBaseList: DataBase[];
  currentDatabase: DataBase | null;
}

export const dataSettings = {
  state: {
    searchContent: '',
    pagination: {
      pageNumber: 1,
      pageSize: 25,
      totalCount: 0,
    },
    dataBaseList: [],
    currentDatabase: null,
  } as DataSettingsState,
  reducers: {
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
  effects: (dispatch: RootDispatch) => ({
    async searchDataBases(_payload: any, rootState: RootState) {
      const { searchContent, pagination } = rootState.dataSettings;
      const resp = await searchDataBasesService(searchContent, pagination);
      if (resp) {
        const { databases, pageNumber, pageSize, totalCount } = resp;
        dispatch.dataSettings.batchUpdateState({
          dataBaseList: databases as DataBase[],
          pagination: {
            pageSize,
            pageNumber,
            totalCount,
          },
        });
      }
    },

    async addDatabase(newDatabase: DatabaseInfo) {
      const resp = await addDatabaseService(newDatabase);
      if (resp) {
        return resp;
      }
      return null;
    },

    async updateDatabase(newDatabase: UpdateDatabaseInfo) {
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
  }),
};
