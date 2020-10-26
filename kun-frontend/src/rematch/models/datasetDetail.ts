import { message } from 'antd';
import { formatMessage } from 'umi';
import {
  fetchDatasetDetailService,
  fetchDatasetColumnsService,
  pullDatasetService,
  UpdateDatasetReqBody,
  updateDatasetService,
  updateColumnService,
} from '@/services/datasetDetail';
import { Pagination } from '@/definitions/common-types';
import { Watermark, GlossaryItem } from '@/definitions/Dataset.type';
import {
  FetchLineageTasksReq,
  fetchLineageTasksService,
  LineageDirection,
} from '@/services/lineage';
import {
  deleteQualityService,
  fetchDataAllQualitiesService,
} from '@/services/dataQuality';
import { DataQualityType } from './dataQuality';
import { RootDispatch, RootState } from '../store';

export interface Flow {
  flow_id: string;
  flow_name: string;
}

export enum DataQualityHistory {
  SUCCESS = 'SUCCESS',
  FAILED = 'FAILED',
  SKIPPED = 'SKIPPED',
}

export interface DataQualityItem {
  id: string;
  name: string;
  updater: string;
  types: DataQualityType[];
  historyList: DataQualityHistory[];
}

export interface Column {
  id: string;
  name: string;
  type: string;
  highWatermark: Watermark;
  description: string;
  not_null_count: number;
  not_null_percentage: number;
}

export interface Glossary {
  id: string;
  name: string;
}

export interface LineageTask {
  taskId: string;
  taskName: string;
  lastExecutedTime: number;
  historyList: DataQualityHistory[];
}

export interface DatasetDetail {
  id: string | null;
  name: string | null;
  schema: string | null;
  type: string | null;
  highWatermark: Watermark | null;
  lowWatermark: Watermark | null;
  description: string | null;

  owners: string[] | null;
  tags: string[] | null;
  datasource: string | null;
  database: string | null;

  rowCount: number | null;
  flows: Flow[] | null;

  dataQualities: DataQualityItem[] | null;
  glossaries: GlossaryItem[] | null;
}

export interface DatasetDetailState extends DatasetDetail {
  columns?: Column[];
  columnsPagination: Pagination;
  columnsKeyword: string;

  dataQualityTablePagination: Pagination;
  fetchDataQualityLoading: boolean;

  upstreamLineageTaskList: LineageTask[] | null;
  downstreamLineageTaskList: LineageTask[] | null;

  fetchUpstreamLineageTaskListLoading: boolean;
  fetchDownstreamLineageTaskListLoading: boolean;

  upstreamLineageTaskListPagination: Pagination;
  downstreamLineageTaskListPagination: Pagination;
}

export const datasetDetail = {
  state: {
    id: null,
    name: null,
    schema: null,
    type: null,
    highWatermark: null,
    lowWatermark: null,
    description: null,

    owners: null,
    tags: null,
    datasource: null,
    database: null,

    rowCount: null,
    flows: null,

    dataQualities: null,
    glossaries: null,

    columns: [],
    columnsPagination: {
      pageNumber: 1,
      pageSize: 25,
      totalCount: 0,
    },
    columnsKeyword: '',
    dataQualityTablePagination: {
      pageNumber: 1,
      pageSize: 25,
      totalCount: 0,
    },
    fetchDataQualityLoading: false,

    upstreamLineageTaskList: [],
    upstreamLineageTaskListPagination: {
      pageNumber: 1,
      pageSize: 25,
      totalCount: 0,
    },
    downstreamLineageTaskList: [],
    downstreamLineageTaskListPagination: {
      pageNumber: 1,
      pageSize: 25,
      totalCount: 0,
    },

    fetchUpstreamLineageTaskListLoading: false,
    fetchDownstreamLineageTaskListLoading: false,
  } as DatasetDetailState,

  reducers: {
    updateState: (
      state: DatasetDetailState,
      payload: { key: keyof DatasetDetailState; value: any },
    ) => ({
      ...state,
      [payload.key]: payload.value,
    }),
    batchUpdateState: (
      state: DatasetDetailState,
      payload: Partial<DatasetDetailState>,
    ) => ({
      ...state,
      ...payload,
    }),
    updatePagination: (
      state: DatasetDetailState,
      payload: Partial<Pagination>,
    ) => ({
      ...state,
      columnsPagination: {
        ...state.columnsPagination,
        ...payload,
      },
    }),
    updateDataQualityPagination: (
      state: DatasetDetailState,
      payload: Partial<Pagination>,
    ) => ({
      ...state,
      dataQualityTablePagination: {
        ...state.dataQualityTablePagination,
        ...payload,
      },
    }),
    updateUpstreamPagination: (
      state: DatasetDetailState,
      payload: Partial<Pagination>,
    ) => ({
      ...state,
      upstreamLineageTaskListPagination: {
        ...state.upstreamLineageTaskListPagination,
        ...payload,
      },
    }),
    updateDownstreamPagination: (
      state: DatasetDetailState,
      payload: Partial<Pagination>,
    ) => ({
      ...state,
      downstreamLineageTaskListPagination: {
        ...state.downstreamLineageTaskListPagination,
        ...payload,
      },
    }),
  },

  effects: (dispatch: RootDispatch) => {
    let fetchDatasetColumnsServiceCountFlag = 1;
    let fetchDataQualityServiceCountFlag = 1;
    let fetchUpstreamLineageTaskListFlag = 1;
    let fetchDownstreamLineageTaskListFlag = 1;
    return {
      async fetchDatasetDetail(id: string) {
        try {
          const resp = await fetchDatasetDetailService(id);
          if (resp) {
            dispatch.datasetDetail.batchUpdateState(resp);
          }
        } catch (e) {
          // do nothing
        }
      },
      async fetchLineageTasks(payload: FetchLineageTasksReq) {
        try {
          let flag;
          if (payload.direction === LineageDirection.UPSTREAM) {
            fetchUpstreamLineageTaskListFlag += 1;
            flag = fetchUpstreamLineageTaskListFlag;
            dispatch.datasetDetail.updateState({
              key: 'fetchUpstreamLineageTaskListLoading',
              value: true,
            });
          } else {
            fetchDownstreamLineageTaskListFlag += 1;
            flag = fetchDownstreamLineageTaskListFlag;
            dispatch.datasetDetail.updateState({
              key: 'fetchDownstreamLineageTaskListLoading',
              value: true,
            });
          }
          const resp = await fetchLineageTasksService(payload);
          if (resp) {
            const { tasks } = resp;
            if (
              payload.direction === LineageDirection.UPSTREAM &&
              flag === fetchUpstreamLineageTaskListFlag
            ) {
              dispatch.datasetDetail.batchUpdateState({
                upstreamLineageTaskList: tasks,
              });
            } else if (flag === fetchDownstreamLineageTaskListFlag) {
              dispatch.datasetDetail.batchUpdateState({
                downstreamLineageTaskList: tasks,
              });
            }
          }
        } catch (e) {
          // do nothing
        } finally {
          if (payload.direction === LineageDirection.UPSTREAM) {
            dispatch.datasetDetail.updateState({
              key: 'fetchUpstreamLineageTaskListLoading',
              value: false,
            });
          } else {
            dispatch.datasetDetail.updateState({
              key: 'fetchDownstreamLineageTaskListLoading',
              value: false,
            });
          }
        }
      },
      async fetchDatasetColumns(payload: {
        id: string;
        keyword: string;
        pagination: Pagination;
      }) {
        const { id, keyword, pagination } = payload;
        fetchDatasetColumnsServiceCountFlag += 1;
        try {
          const currentFetchDatasetColumnsServiceCountFlag = fetchDatasetColumnsServiceCountFlag;
          const resp = await fetchDatasetColumnsService(
            id,
            keyword,
            pagination,
          );
          if (
            currentFetchDatasetColumnsServiceCountFlag ===
            fetchDatasetColumnsServiceCountFlag
          ) {
            if (resp) {
              const { columns, pageNumber, pageSize, totalCount } = resp;

              dispatch.datasetDetail.updateState({
                key: 'columns',
                value: columns,
              });
              dispatch.datasetDetail.updatePagination({
                pageNumber,
                pageSize,
                totalCount,
              });
            }
          }
        } catch (e) {
          // do nothing
        }
      },
      async fetchDataQualities(payload: {
        id: string;
        pagination: Pagination;
      }) {
        const { id, pagination } = payload;
        fetchDataQualityServiceCountFlag += 1;
        try {
          dispatch.datasetDetail.updateState({
            key: 'fetchDataQualityLoading',
            value: true,
          });
          const currentFetchDataQualityServiceCountFlag = fetchDataQualityServiceCountFlag;
          const resp = await fetchDataAllQualitiesService(id, pagination);
          if (
            currentFetchDataQualityServiceCountFlag ===
            fetchDataQualityServiceCountFlag
          ) {
            if (resp) {
              const { dqCases, pageNumber, pageSize, totalCount } = resp;

              dispatch.datasetDetail.updateState({
                key: 'dataQualities',
                value: dqCases,
              });
              dispatch.datasetDetail.updateDataQualityPagination({
                pageNumber,
                pageSize,
                totalCount,
              });
            }
          }
        } catch (e) {
          // do nothing
        } finally {
          dispatch.datasetDetail.updateState({
            key: 'fetchDataQualityLoading',
            value: false,
          });
        }
      },
      async pullDataset(id: string) {
        try {
          const resp = await pullDatasetService(id);
          if (resp) {
            message.success(
              formatMessage(
                {
                  id: `dataDetail.button.pullDuration`,
                },
                { time: resp.duration },
              ),
            );
            return resp;
          }
        } catch (e) {
          // do nothing
        }
        return null;
      },
      async updateDataset(
        payload: { id: string; updateParams: Partial<UpdateDatasetReqBody> },
        rootState: RootState,
      ) {
        const { id, updateParams } = payload;
        const { description, owners, tags } = rootState.datasetDetail;
        const reqBody = {
          description,
          owners,
          tags,
          ...updateParams,
        };
        try {
          const resp = await updateDatasetService(id, reqBody);
          if (resp) {
            dispatch.datasetDetail.batchUpdateState(resp);
          }
        } catch (e) {
          // do nothing
        }
      },

      async updateColumn(payload: { id: string; description: string }) {
        const { id, description } = payload;
        try {
          const resp = await updateColumnService(id, { description });
          if (resp) {
            return resp;
          }
        } catch (e) {
          // do nothing
        }
        return null;
      },

      async deleteDataQuality(payload: { id: string }) {
        const { id } = payload;
        try {
          const resp = await deleteQualityService(id);
          if (resp) {
            return resp;
          }
        } catch (e) {
          // do nothing
        }
        return null;
      },
    };
  },
};
