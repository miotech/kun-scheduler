import produce from 'immer';
import { DataDevelopmentMetrics, MetadataMetrics } from '@/services/monitoring-dashboard';

import {
  DataDevelopmentBoardFilterCardType,
  initState,
  MonitoringDashboardModelState as ModelState,
} from './model-state';

export const reducers = {
  resetAll: (): ModelState => ({
    ...initState,
  }),
  setAllSettled: produce((draftState: ModelState, payload: boolean) => {
    draftState.allSettled = payload;
  }),
  setDataDiscoveryMetrics: produce((draftState: ModelState, payload: MetadataMetrics | null) => {
    draftState.dataDiscoveryBoardData.metadataMetrics = payload;
  }),
  setDataDiscoveryMetricsLoading: produce((draftState: ModelState, payload: boolean) => {
    draftState.dataDiscoveryBoardData.metadataMetricsLoading = payload;
  }),
  setTopDatasetsWithMaxRowChange: produce(
    (draftState: ModelState, payload: ModelState['dataDiscoveryBoardData']['maxRowCountChange']) => {
      draftState.dataDiscoveryBoardData.maxRowCountChange = payload;
    },
  ),
  updateTopDatasetsWithMaxRowChange: produce(
    (draftState: ModelState, payload: Partial<ModelState['dataDiscoveryBoardData']['maxRowCountChange']>) => {
      draftState.dataDiscoveryBoardData.maxRowCountChange = {
        ...draftState.dataDiscoveryBoardData.maxRowCountChange,
        ...payload,
      };
    },
  ),
  setFailedTestCases: produce(
    (draftState: ModelState, payload: ModelState['dataDiscoveryBoardData']['failedTestCases']) => {
      draftState.dataDiscoveryBoardData.failedTestCases = payload;
    },
  ),
  updateAbnormalDatasets: produce(
    (draftState: ModelState, payload: Partial<ModelState['dataDiscoveryBoardData']['failedTestCases']>) => {
      draftState.dataDiscoveryBoardData.failedTestCases = {
        ...draftState.dataDiscoveryBoardData.failedTestCases,
        ...payload,
      };
    },
  ),
  setDatasetMetrics: produce(
    (draftState: ModelState, payload: ModelState['dataDiscoveryBoardData']['datasetMetrics']) => {
      draftState.dataDiscoveryBoardData.datasetMetrics = payload;
    },
  ),
  updateDatasetMetrics: produce(
    (draftState: ModelState, payload: Partial<ModelState['dataDiscoveryBoardData']['datasetMetrics']>) => {
      draftState.dataDiscoveryBoardData.datasetMetrics = {
        ...draftState.dataDiscoveryBoardData.datasetMetrics,
        ...payload,
      };
    },
  ),
  setDataDevelopmentMetrics: produce((draftState: ModelState, payload: DataDevelopmentMetrics) => {
    draftState.dataDevelopmentBoardData.dataDevelopmentMetrics = {
      ...payload,
    };
  }),
  setDataDevelopmentMetricsLoading: produce((draftState: ModelState, payload: boolean) => {
    draftState.dataDevelopmentBoardData.dataDevelopmentMetricsLoading = payload;
  }),
  setDailyTaskFinish: produce(
    (draftState: ModelState, payload: ModelState['dataDevelopmentBoardData']['dailyTaskFinish']) => {
      draftState.dataDevelopmentBoardData.dailyTaskFinish = payload;
    },
  ),
  updateDailyTaskFinish: produce(
    (draftState: ModelState, payload: Partial<ModelState['dataDevelopmentBoardData']['dailyTaskFinish']>) => {
      draftState.dataDevelopmentBoardData.dailyTaskFinish = {
        ...draftState.dataDevelopmentBoardData.dailyTaskFinish,
        ...payload,
      };
    },
  ),
  setTaskDetails: produce((draftState: ModelState, payload: ModelState['dataDevelopmentBoardData']['taskDetails']) => {
    draftState.dataDevelopmentBoardData.taskDetails = payload;
  }),
  updateTaskDetails: produce(
    (draftState: ModelState, payload: Partial<ModelState['dataDevelopmentBoardData']['taskDetails']>) => {
      draftState.dataDevelopmentBoardData.taskDetails = {
        ...draftState.dataDevelopmentBoardData.taskDetails,
        ...payload,
      };
    },
  ),
  setTaskDetailsSelectedFilter: produce((draftState: ModelState, payload: DataDevelopmentBoardFilterCardType) => {
    draftState.dataDevelopmentBoardData.taskDetailsSelectedFilter = payload;
    draftState.dataDevelopmentBoardData.taskDetailsForWeekParams = null;
    draftState.dataDevelopmentBoardData.clearBarChart = !draftState.dataDevelopmentBoardData.clearBarChart;
  }),
  setTaskDetailsDisplayStartedOnly: produce((draftState: ModelState, payload: boolean) => {
    draftState.dataDevelopmentBoardData.taskDetailsDisplayStartedOnly = payload;
    draftState.dataDevelopmentBoardData.taskDetailsForWeekParams = null;
  }),
  setTaskDetailsDisplayLast24HoursOnly: produce((draftState: ModelState, payload: boolean) => {
    draftState.dataDevelopmentBoardData.taskDetailsDisplayLast24HoursOnly = payload;
    draftState.dataDevelopmentBoardData.taskDetailsForWeekParams = null;
  }),
  setTaskDetailsForWeekParams: produce((draftState: ModelState, payload: any) => {
    draftState.dataDevelopmentBoardData.taskDetailsForWeekParams = payload;
    if(!payload.type){
      draftState.dataDevelopmentBoardData.clearBarChart = !draftState.dataDevelopmentBoardData.clearBarChart;
    }
    draftState.dataDevelopmentBoardData.taskDetails.pageNum = 1;
    draftState.dataDevelopmentBoardData.taskDetailsSelectedFilter = null;
  }),

  updateStatisticChartData: produce((draftState: ModelState, payload) => {
    draftState.dataDevelopmentBoardData.dailyStatisticList = {
      ...draftState.dataDevelopmentBoardData.dailyStatisticList,
      ...payload,
    };
  }),
};
