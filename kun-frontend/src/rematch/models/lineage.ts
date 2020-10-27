import { fetchDatasetDetailService } from '@/services/datasetDetail';
import {
  FetchLineageTasksReq,
  LineageDirection,
  fetchLineageTasksService,
  fetchLineageGraphInfoService,
} from '@/services/lineage';
import { Vertex, Edge } from '@/definitions/Dataset.type';
import { LineageTask, DatasetDetail } from './datasetDetail';
import { RootDispatch } from '../store';

export interface LineageState {
  activeDatasetDetail: DatasetDetail | null;
  activeDatasetDetailLoading: boolean;

  activeUpstreamLineageTaskList: LineageTask[] | null;
  activeDownstreamLineageTaskList: LineageTask[] | null;

  activeFetchUpstreamLineageTaskListLoading: boolean;
  activeFetchDownstreamLineageTaskListLoading: boolean;

  graph: {
    vertices: Vertex[];
    edges: Edge[];
  };
  graphLoading: boolean;

  selectedNodeId: string | null;

  selectedEdgeInfo: {
    sourceNodeId: string;
    destNodeId: string;
    sourceNodeName: string;
    destNodeName: string;
  } | null;
}

export const lineage = {
  state: {
    activeDatasetDetail: null,

    activeDatasetDetailLoading: false,

    activeUpstreamLineageTaskList: [],
    activeDownstreamLineageTaskList: [],

    activeFetchUpstreamLineageTaskListLoading: false,
    activeFetchDownstreamLineageTaskListLoading: false,

    graph: {
      vertices: [],
      edges: [],
    },

    graphLoading: false,

    selectedNodeId: null,
    selectedEdgeInfo: null,
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
    updateGraph: (
      state: LineageState,
      payload: Partial<LineageState['graph']>,
    ) => ({
      ...state,
      graph: {
        ...state.graph,
        ...payload,
      },
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

      async fetchInitialLineageGraphInfo(id: string) {
        try {
          dispatch.lineage.updateState({
            key: 'graphLoading',
            value: true,
          });
          const resp = await fetchLineageGraphInfoService({ datasetGid: id });
          if (resp) {
            dispatch.lineage.updateGraph(resp);
          }
        } catch (e) {
          // do nothing
        } finally {
          dispatch.lineage.updateState({
            key: 'graphLoading',
            value: false,
          });
        }
      },
    };
  },
};
