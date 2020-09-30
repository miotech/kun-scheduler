import produce from 'immer';
import { DataDevelopmentMetrics, MetadataMetrics } from '@/services/monitoring-dashboard';

import {
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
  setTopDatasetsWithMaxRowChange: produce((draftState: ModelState, payload: ModelState['dataDiscoveryBoardData']['maxRowCountChange']) => {
    draftState.dataDiscoveryBoardData.maxRowCountChange = payload;
  }),
  setFailedTestCases: produce((draftState: ModelState, payload: ModelState['dataDiscoveryBoardData']['failedTestCases']) => {
    draftState.dataDiscoveryBoardData.failedTestCases = payload;
  }),
  updateFailedTestCases: produce((draftState: ModelState, payload: Partial<ModelState['dataDiscoveryBoardData']['failedTestCases']>) => {
    draftState.dataDiscoveryBoardData.failedTestCases = {
      ...draftState.dataDiscoveryBoardData.failedTestCases,
      ...payload,
    };
  }),
  setDatasetMetrics: produce((draftState: ModelState, payload: ModelState['dataDiscoveryBoardData']['datasetMetrics']) => {
    draftState.dataDiscoveryBoardData.datasetMetrics = payload;
  }),
  updateDatasetMetrics: produce((draftState: ModelState, payload: Partial<ModelState['dataDiscoveryBoardData']['datasetMetrics']>) => {
    draftState.dataDiscoveryBoardData.datasetMetrics = {
      ...draftState.dataDiscoveryBoardData.datasetMetrics,
      ...payload,
    };
  }),
  setDataDevelopmentMetrics: produce((draftState: ModelState, payload: DataDevelopmentMetrics) => {
    draftState.dataDevelopmentBoardData.dataDevelopmentMetrics = {
      ...payload,
    };
  }),
  setDailyTaskFinish: produce((draftState: ModelState, payload: ModelState['dataDevelopmentBoardData']['dailyTaskFinish']) => {
    draftState.dataDevelopmentBoardData.dailyTaskFinish = payload;
  }),
  updateDailyTaskFinish: produce((draftState: ModelState, payload: Partial<ModelState['dataDevelopmentBoardData']['dailyTaskFinish']>) => {
    draftState.dataDevelopmentBoardData.dailyTaskFinish = {
      ...draftState.dataDevelopmentBoardData.dailyTaskFinish,
      ...payload,
    };
  }),
  setTaskDetails: produce((draftState: ModelState, payload: ModelState['dataDevelopmentBoardData']['taskDetails']) => {
    draftState.dataDevelopmentBoardData.taskDetails = payload;
  }),
  updateTaskDetails: produce((draftState: ModelState, payload: Partial<ModelState['dataDevelopmentBoardData']['taskDetails']>) => {
    draftState.dataDevelopmentBoardData.taskDetails = {
      ...draftState.dataDevelopmentBoardData.taskDetails,
      ...payload,
    };
  }),
};
