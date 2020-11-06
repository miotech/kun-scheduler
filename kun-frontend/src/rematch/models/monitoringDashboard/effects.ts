import { RootDispatch } from '@/rematch/store';
import * as services from '@/services/monitoring-dashboard';
import { MonitoringDashboardModelState as ModelState } from '@/rematch/models/monitoringDashboard/model-state';
import {
  DataDevelopmentMetrics,
  DatasetColumnMetricsInfo,
  FailedTestCasesInfo,
  MetadataMetrics,
} from '@/services/monitoring-dashboard';
import { PaginationReqBody, SortReqBody } from '@/definitions/common-types';

let loadDataDiscoveryMetricsFlag = 0;
let loadTopDatasetsMaxRowCountChangeFlag = 0;
let loadFailedTestCasesFlag = 0;
let loadDatasetMetricsFlag = 0;
let loadDataDevelopmentMetricsFlag = 0;
let loadDailyTaskFinishFlag = 0;
let loadTaskDetailsFlag = 0;

// Data discovery board: fetch top metrics data
const loadDataDiscoveryMetrics = (dispatch: RootDispatch) => {
  dispatch.monitoringDashboard.setDataDiscoveryMetricsLoading(true);
  loadDataDiscoveryMetricsFlag += 1;
  const currentFlag = loadDataDiscoveryMetricsFlag;
  return services
    .fetchMetadataMetrics()
    .then((metrics: MetadataMetrics) => {
      if (currentFlag === loadDataDiscoveryMetricsFlag) {
        dispatch.monitoringDashboard.setDataDiscoveryMetrics(metrics || null);
      }
    })
    .catch(() => {
      if (currentFlag === loadDataDiscoveryMetricsFlag) {
        dispatch.monitoringDashboard.setDataDiscoveryMetrics(null);
      }
    })
    .finally(() => {
      if (currentFlag === loadDataDiscoveryMetricsFlag) {
        dispatch.monitoringDashboard.setDataDiscoveryMetricsLoading(false);
      }
    });
};

// Data discovery board: fetch top 10 datasets with Max Row Count Change
const loadTopDatasetsMaxRowCountChange = (dispatch: RootDispatch) => {
  dispatch.monitoringDashboard.updateTopDatasetsWithMaxRowChange({
    loading: true,
  });

  loadTopDatasetsMaxRowCountChangeFlag += 1;
  const currentFlag = loadTopDatasetsMaxRowCountChangeFlag;

  services
    .fetchMaxRowCountChange({
      pageSize: 10,
    })
    .then(fetchedData => {
      if (currentFlag === loadTopDatasetsMaxRowCountChangeFlag) {
        dispatch.monitoringDashboard.setTopDatasetsWithMaxRowChange({
          data: fetchedData.rowCountChanges,
          error: null,
          loading: false,
        });
      }
    })
    .catch(e => {
      if (currentFlag === loadTopDatasetsMaxRowCountChangeFlag) {
        dispatch.monitoringDashboard.setTopDatasetsWithMaxRowChange({
          data: [],
          error: e,
          loading: false,
        });
      }
    });
};

// Data discovery board: fetch failed test cases
const loadFailedTestCases = (
  params: PaginationReqBody & Partial<SortReqBody>,
  dispatch: RootDispatch,
) => {
  dispatch.monitoringDashboard.updateFailedTestCases({
    loading: true,
  });

  loadFailedTestCasesFlag += 1;
  const currentFlag = loadFailedTestCasesFlag;

  return services
    .fetchFailedTestCases({
      pageNumber: params.pageNumber,
      pageSize: params.pageSize,
      sortColumn: (params.sortColumn as any) || undefined,
      sortOrder: params.sortOrder || undefined,
    })
    .then((fetchedData: FailedTestCasesInfo) => {
      if (currentFlag === loadFailedTestCasesFlag) {
        dispatch.monitoringDashboard.updateFailedTestCases({
          loading: false,
          data: fetchedData.dataQualityCases,
          total: fetchedData.totalCount,
          error: null,
        });
      }
    })
    .catch(e => {
      if (currentFlag === loadFailedTestCasesFlag) {
        dispatch.monitoringDashboard.updateFailedTestCases({
          loading: false,
          data: [],
          error: e,
        });
      }
    });
};

// Data discovery board: fetch datasets column metrics
const loadDatasetMetrics = (
  params: PaginationReqBody,
  dispatch: RootDispatch,
) => {
  dispatch.monitoringDashboard.updateDatasetMetrics({
    loading: true,
  });

  loadDatasetMetricsFlag += 1;
  const currentFlag = loadDatasetMetricsFlag;

  return services
    .fetchDatasetColumnMetrics({
      pageNumber: params.pageNumber,
      pageSize: params.pageSize,
    })
    .then((fetchedData: DatasetColumnMetricsInfo) => {
      if (currentFlag === loadDatasetMetricsFlag) {
        dispatch.monitoringDashboard.updateDatasetMetrics({
          loading: false,
          error: null,
          data: fetchedData.columnMetricsList,
          total: fetchedData.totalCount,
        });
      }
    })
    .catch(e => {
      if (currentFlag === loadDatasetMetricsFlag) {
        dispatch.monitoringDashboard.updateDatasetMetrics({
          loading: false,
          error: e,
          data: [],
        });
      }
    });
};

// Data Development board
const loadDataDevelopmentMetrics = (dispatch: RootDispatch) => {
  dispatch.monitoringDashboard.setDataDevelopmentMetricsLoading(true);

  loadDataDevelopmentMetricsFlag += 1;
  const currentFlag = loadDataDevelopmentMetricsFlag;

  return services
    .fetchDataDevelopmentMetrics()
    .then((fetchedData: DataDevelopmentMetrics) => {
      if (currentFlag === loadDataDevelopmentMetricsFlag) {
        dispatch.monitoringDashboard.setDataDevelopmentMetrics(fetchedData);
      }
    })
    .catch(() => {})
    .finally(() => {
      if (currentFlag === loadDataDevelopmentMetricsFlag) {
        dispatch.monitoringDashboard.setDataDevelopmentMetricsLoading(false);
      }
    });
};

// daily task finishes
const loadDailyTaskFinish = (dispatch: RootDispatch) => {
  dispatch.monitoringDashboard.updateDailyTaskFinish({
    loading: true,
  });

  loadDailyTaskFinishFlag += 1;
  const currentFlag = loadDailyTaskFinishFlag;

  return services
    .fetchDataDevelopmentDailyTaskCount()
    .then(fetchedData => {
      if (currentFlag === loadDailyTaskFinishFlag) {
        dispatch.monitoringDashboard.setDailyTaskFinish({
          loading: false,
          data: fetchedData.taskCountList,
          error: null,
        });
      }
    })
    .catch(e => {
      if (currentFlag === loadDailyTaskFinishFlag) {
        dispatch.monitoringDashboard.setDailyTaskFinish({
          loading: false,
          data: [],
          error: e,
        });
      }
    });
};

// task details
const loadTaskDetails = (params: PaginationReqBody, dispatch: RootDispatch) => {
  dispatch.monitoringDashboard.updateTaskDetails({
    loading: true,
  });

  loadTaskDetailsFlag += 1;
  const currentFlag = loadTaskDetailsFlag;

  return services
    .fetchDataDevelopmentTaskDetails(params)
    .then(fetchedData => {
      if (currentFlag === loadTaskDetailsFlag) {
        dispatch.monitoringDashboard.updateTaskDetails({
          data: fetchedData.tasks,
          loading: false,
          error: null,
          total: fetchedData.totalCount,
        });
      }
    })
    .catch(e => {
      if (currentFlag === loadTaskDetailsFlag) {
        dispatch.monitoringDashboard.updateTaskDetails({
          data: [],
          loading: false,
          error: e,
        });
      }
    });
};

/**
 * Store effects for monitoring dashboard
 * @param dispatch
 */
export const effects = (dispatch: RootDispatch) => ({
  async reloadAll(modelState: ModelState) {
    const {
      failedTestCases,
      datasetMetrics,
    } = modelState.dataDiscoveryBoardData;

    const { taskDetails } = modelState.dataDevelopmentBoardData;

    dispatch.monitoringDashboard.setAllSettled(false);
    Promise.allSettled([
      loadDataDiscoveryMetrics(dispatch),
      loadTopDatasetsMaxRowCountChange(dispatch),
      loadFailedTestCases(
        {
          pageNumber: failedTestCases.pageNum,
          pageSize: failedTestCases.pageSize,
          sortOrder: failedTestCases.sortOrder || undefined,
          sortColumn: failedTestCases.sortColumn || undefined,
        },
        dispatch,
      ),
      loadDatasetMetrics(
        {
          pageNumber: datasetMetrics.pageNum,
          pageSize: datasetMetrics.pageSize,
        },
        dispatch,
      ),
      loadDataDevelopmentMetrics(dispatch),
      loadDailyTaskFinish(dispatch),
      loadTaskDetails(
        {
          pageNumber: taskDetails.pageNum,
          pageSize: taskDetails.pageSize,
        },
        dispatch,
      ),
    ]).finally(() => {
      dispatch.monitoringDashboard.setAllSettled(true);
    });
  },
  async reloadDataDiscoveryMetrics() {
    await loadDataDiscoveryMetrics(dispatch);
  },
  async reloadDatasetsWithMaxRowCountChange() {
    await loadTopDatasetsMaxRowCountChange(dispatch);
  },
  async reloadFailedTestCases(
    params: PaginationReqBody & Partial<SortReqBody>,
  ) {
    await loadFailedTestCases(params, dispatch);
  },
  async reloadDatasetMetrics(params: PaginationReqBody) {
    await loadDatasetMetrics(params, dispatch);
  },
  async reloadDataDevelopmentMetrics() {
    await loadDataDevelopmentMetrics(dispatch);
  },
  async reloadDailyTaskFinish() {
    await loadDailyTaskFinish(dispatch);
  },
  async reloadTaskDetails(params: PaginationReqBody) {
    await loadTaskDetails(params, dispatch);
  },
});
