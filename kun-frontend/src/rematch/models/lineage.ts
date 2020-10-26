import { fetchDatasetDetailService } from '@/services/datasetDetail';
import {
  FetchLineageTasksReq,
  LineageDirection,
  fetchLineageTasksService,
} from '@/services/lineage';
import { LineageTask, DatasetDetail } from './datasetDetail';
import { RootDispatch } from '../store';

export interface LineageState {
  activeDatasetDetail: DatasetDetail | null;
  activeDatasetDetailLoading: boolean;

  activeUpstreamLineageTaskList: LineageTask[] | null;
  activeDownstreamLineageTaskList: LineageTask[] | null;

  activeFetchUpstreamLineageTaskListLoading: boolean;
  activeFetchDownstreamLineageTaskListLoading: boolean;
}

export const lineage = {
  state: {
    activeDatasetDetail: null,

    activeDatasetDetailLoading: false,

    activeUpstreamLineageTaskList: [],
    activeDownstreamLineageTaskList: [],

    activeFetchUpstreamLineageTaskListLoading: false,
    activeFetchDownstreamLineageTaskListLoading: false,
  } as LineageState,
  reducers: {
    updateState: (
      state: LineageState,
      payload: { key: keyof LineageState; value: any },
    ) => ({
      ...state,
      [payload.key]: payload.value,
    }),
    batchUpdateState: (
      state: LineageState,
      payload: Partial<LineageState>,
    ) => ({
      ...state,
      ...payload,
    }),
  },

  effects: (dispatch: RootDispatch) => {
    return {
      async fetchDatasetDetail(id: string) {
        try {
          dispatch.lineage.updateState({
            key: 'activeDatasetDetailLoading',
            value: true,
          });
          const resp = await fetchDatasetDetailService(id);
          if (resp) {
            dispatch.lineage.updateState({
              key: 'activeDatasetDetail',
              value: resp,
            });
          }
        } catch (e) {
          // do nothing
        } finally {
          dispatch.lineage.updateState({
            key: 'activeDatasetDetailLoading',
            value: false,
          });
        }
      },

      async fetchLineageTasks(payload: FetchLineageTasksReq) {
        try {
          if (payload.direction === LineageDirection.UPSTREAM) {
            dispatch.lineage.updateState({
              key: 'activeFetchUpstreamLineageTaskListLoading',
              value: true,
            });
          } else {
            dispatch.lineage.updateState({
              key: 'activeFetchDownstreamLineageTaskListLoading',
              value: true,
            });
          }
          const resp = await fetchLineageTasksService(payload);
          if (resp) {
            const { tasks } = resp;
            if (payload.direction === LineageDirection.UPSTREAM) {
              dispatch.lineage.updateState({
                key: 'activeUpstreamLineageTaskList',
                value: tasks,
              });
            } else {
              dispatch.lineage.updateState({
                key: 'activeDownstreamLineageTaskList',
                value: tasks,
              });
            }
          }
        } catch (e) {
          // do nothing
        } finally {
          if (payload.direction === LineageDirection.UPSTREAM) {
            dispatch.lineage.updateState({
              key: 'activeFetchUpstreamLineageTaskListLoading',
              value: false,
            });
          } else {
            dispatch.lineage.updateState({
              key: 'activeFetchDownstreamLineageTaskListLoading',
              value: false,
            });
          }
        }
      },
    };
  },
};
