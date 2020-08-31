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
import { deleteQualityService } from '@/services/dataQuality';
import { Watermark, GlossaryItem } from './dataDiscovery';
import { RootDispatch, RootState } from '../store';

export interface Flow {
  flow_id: string;
  flow_name: string;
}

export interface DataQualityItem {
  id: string;
  name: string;
}

export interface Column {
  id: string;
  name: string;
  type: string;
  high_watermark: Watermark;
  description: string;
  not_null_count: number;
  not_null_percentage: number;
}

export interface DatasetDetail {
  id: string | null;
  name: string | null;
  schema: string | null;
  type: string | null;
  high_watermark: Watermark | null;
  low_watermark: Watermark | null;
  description: string | null;

  owners: string[] | null;
  tags: string[] | null;
  glossaries: GlossaryItem[] | null;
  datasource: string | null;
  database: string | null;

  row_count: number | null;
  flows: Flow[] | null;

  dataQualities: DataQualityItem[] | null;
}

export interface DatasetDetailState extends DatasetDetail {
  columns?: Column[];
  columnsPagination: Pagination;
  columnsKeyword: string;
}

export const datasetDetail = {
  state: {
    id: null,
    name: null,
    schema: null,
    type: null,
    high_watermark: null,
    low_watermark: null,
    description: null,

    owners: null,
    tags: null,
    glossaries: null,
    datasource: null,
    database: null,

    row_count: null,
    flows: null,

    dataQualities: null,

    columns: [],
    columnsPagination: {
      pageNumber: 1,
      pageSize: 25,
      totalCount: 0,
    },
    columnsKeyword: '',
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
  },

  effects: (dispatch: RootDispatch) => {
    let fetchDatasetColumnsServiceCountFlag = 1;
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

      async deleteDataQuality(payload: { id: string; datasetId: string }) {
        const { id, datasetId } = payload;
        try {
          const resp = await deleteQualityService(id, { datasetId });
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
