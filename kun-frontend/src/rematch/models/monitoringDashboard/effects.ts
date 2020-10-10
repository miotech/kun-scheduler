import { RootDispatch } from '@/rematch/store';
import * as services from '@/services/monitoring-dashboard';
import { MonitoringDashboardModelState as ModelState } from '@/rematch/models/monitoringDashboard/model-state';
import {
  DataDevelopmentMetrics, DatasetColumnMetricsInfo, FailedTestCasesInfo, MetadataMetrics
} from '@/services/monitoring-dashboard';
import { PaginationReqBody, SortReqBody } from '@/definitions/common-types';

// Data discovery board: fetch top metrics data
const loadDataDiscoveryMetrics = (dispatch: RootDispatch) => {
  dispatch.monitoringDashboard.setDataDiscoveryMetricsLoading(true);
  return services.fetchMetadataMetrics()
    .then((metrics: MetadataMetrics) => {
      dispatch.monitoringDashboard.setDataDiscoveryMetrics(metrics || null);
    }).catch(() => {
      dispatch.monitoringDashboard.setDataDiscoveryMetrics(null);
    }).finally(() => {
      dispatch.monitoringDashboard.setDataDiscoveryMetricsLoading(false);
    });
}

// Data discovery board: fetch top 10 datasets with Max Row Count Change
const loadTopDatasetsMaxRowCountChange = (dispatch: RootDispatch) => {
  dispatch.monitoringDashboard.updateTopDatasetsWithMaxRowChange({
    loading: true,
  });
  services.fetchMaxRowCountChange({
    pageSize: 10,
  }).then(fetchedData => {
    dispatch.monitoringDashboard.setTopDatasetsWithMaxRowChange({
      data: fetchedData.rowCountChanges,
      error: null,
      loading: false,
    });
  }).catch((e) => {
    dispatch.monitoringDashboard.setTopDatasetsWithMaxRowChange({
      data: [],
      error: e,
      loading: false,
    });
  });
};

// Data discovery board: fetch failed test cases
const loadFailedTestCases = (params: PaginationReqBody & Partial<SortReqBody>, dispatch: RootDispatch) => {
  dispatch.monitoringDashboard.updateFailedTestCases({
    loading: true,
  });
  return services.fetchFailedTestCases({
    pageNumber: params.pageNumber,
    pageSize: params.pageSize,
    sortColumn: params.sortColumn as any || undefined,
    sortOrder: params.sortOrder || undefined,
  }).then((fetchedData: FailedTestCasesInfo) => {
    dispatch.monitoringDashboard.updateFailedTestCases({
      loading: false,
      data: fetchedData.testCases,
      total: fetchedData.totalCount,
      error: null,
    });
  }).catch(e => {
    dispatch.monitoringDashboard.updateFailedTestCases({
      loading: false,
      data: [],
      error: e,
    });
  });
}

// Data discovery board: fetch datasets column metrics
const loadDatasetMetrics = (params: PaginationReqBody, dispatch: RootDispatch) => {
  dispatch.monitoringDashboard.updateDatasetMetrics({
    loading: true,
  })
  return services.fetchDatasetColumnMetrics({
    pageNumber: params.pageNumber,
    pageSize: params.pageSize,
  }).then((fetchedData: DatasetColumnMetricsInfo) => {
    dispatch.monitoringDashboard.updateDatasetMetrics({
      loading: false,
      error: null,
      data: fetchedData.columnMetricsList,
      total: fetchedData.totalCount,
    });
  }).catch((e) => {
    dispatch.monitoringDashboard.updateDatasetMetrics({
      loading: false,
      error: e,
      data: [],
    });
  });
}

// Data Development board
const loadDataDevelopmentMetrics = (dispatch: RootDispatch) => {
  dispatch.monitoringDashboard.setDataDevelopmentMetricsLoading(true);
  return services.fetchDataDevelopmentMetrics()
    .then((fetchedData: DataDevelopmentMetrics) => {
      dispatch.monitoringDashboard.setDataDevelopmentMetrics(fetchedData);
    })
    .catch(() => {
    })
    .finally(() => {
      dispatch.monitoringDashboard.setDataDevelopmentMetricsLoading(false);
    });
}

// daily task finishes
const loadDailyTaskFinish = (dispatch: RootDispatch) => {
  dispatch.monitoringDashboard.updateDailyTaskFinish({
    loading: true,
  });
  return services.fetchDataDevelopmentDailyTaskCount()
    .then(fetchedData => {
      dispatch.monitoringDashboard.setDailyTaskFinish({
        loading: false,
        data: fetchedData.taskCountList,
        error: null,
      });
    })
    .catch((e) => {
      dispatch.monitoringDashboard.setDailyTaskFinish({
        loading: false,
        data: [],
        error: e,
      });
    });
}

// task details
const loadTaskDetails = (params: PaginationReqBody, dispatch: RootDispatch) => {
  dispatch.monitoringDashboard.updateTaskDetails({
    loading: true,
  });
  return services.fetchDataDevelopmentTaskDetails(params)
    .then(fetchedData => {
      dispatch.monitoringDashboard.updateTaskDetails({
        data: fetchedData.tasks,
        loading: false,
        error: null,
        total: fetchedData.totalCount,
      });
    })
    .catch((e) => {
      dispatch.monitoringDashboard.updateTaskDetails({
        data: [],
        loading: false,
        error: e,
      });
    });
}

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

    const {
      taskDetails,
    } = modelState.dataDevelopmentBoardData;

    dispatch.monitoringDashboard.setAllSettled(false);
    Promise.allSettled([
      loadDataDiscoveryMetrics(dispatch),
      loadTopDatasetsMaxRowCountChange(dispatch),
      loadFailedTestCases({
        pageNumber: failedTestCases.pageNum,
        pageSize: failedTestCases.pageSize,
        sortOrder: failedTestCases.sortOrder || undefined,
        sortColumn: failedTestCases.sortColumn || undefined,
      }, dispatch),
      loadDatasetMetrics({
        pageNumber: datasetMetrics.pageNum,
        pageSize: datasetMetrics.pageSize,
      }, dispatch),
      loadDataDevelopmentMetrics(dispatch),
      loadDailyTaskFinish(dispatch),
      loadTaskDetails({
        pageNumber: taskDetails.pageNum,
        pageSize: taskDetails.pageSize,
      }, dispatch),
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
  async reloadFailedTestCases(params: PaginationReqBody & Partial<SortReqBody>) {
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
  }
});
