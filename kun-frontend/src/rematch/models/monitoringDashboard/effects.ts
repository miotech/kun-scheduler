import { RootDispatch } from '@/rematch/store';
import * as services from '@/services/monitoring-dashboard';
import {
  DataDevelopmentMetrics,
  DatasetColumnMetricsInfo,
  MetadataMetrics,
  AbnormalDatasetDatasetInfo,
} from '@/services/monitoring-dashboard';
import { PaginationReqBody, SortReqBody } from '@/definitions/common-types';

let loadDataDiscoveryMetricsFlag = 0;
let loadTopDatasetsMaxRowCountChangeFlag = 0;
let loadFailedTestCasesFlag = 0;
let loadDatasetMetricsFlag = 0;
let loadDataDevelopmentMetricsFlag = 0;
let loadTaskDetailsFlag = 0;

// Data discovery board: fetch top metrics data
const loadDataDiscoveryMetrics = (dispatch: RootDispatch, showloading = true) => {
  if (showloading) {
    dispatch.monitoringDashboard.setDataDiscoveryMetricsLoading(true);
  }
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
const loadTopDatasetsMaxRowCountChange = (dispatch: RootDispatch, showloading = true) => {
  if (showloading) {
    dispatch.monitoringDashboard.updateTopDatasetsWithMaxRowChange({
      loading: true,
    });
  }

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
  showloading = true,
) => {
  if (showloading) {
    dispatch.monitoringDashboard.updateAbnormalDatasets({
      loading: true,
    });
  }
  loadFailedTestCasesFlag += 1;
  const currentFlag = loadFailedTestCasesFlag;

  return services
    .fetchFailedTestCases({
      pageNumber: params.pageNumber,
      pageSize: params.pageSize,
      sortColumn: (params.sortColumn as any) || undefined,
      sortOrder: params.sortOrder || undefined,
    })
    .then((fetchedData: AbnormalDatasetDatasetInfo) => {
      if (currentFlag === loadFailedTestCasesFlag) {
        dispatch.monitoringDashboard.updateAbnormalDatasets({
          loading: false,
          data: fetchedData.abnormalDatasets,
          total: fetchedData.totalCount,
          error: null,
        });
      }
    })
    .catch(e => {
      if (currentFlag === loadFailedTestCasesFlag) {
        dispatch.monitoringDashboard.updateAbnormalDatasets({
          loading: false,
          data: [],
          error: e,
        });
      }
    });
};

// Data discovery board: fetch datasets column metrics
const loadDatasetMetrics = (params: PaginationReqBody, dispatch: RootDispatch, showloading = true) => {
  if (showloading) {
    dispatch.monitoringDashboard.updateDatasetMetrics({
      loading: true,
    });
  }

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
const loadDataDevelopmentMetrics = (dispatch: RootDispatch, showloading = true) => {
  if (showloading) {
    dispatch.monitoringDashboard.setDataDevelopmentMetricsLoading(true);
  }
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

export interface LoadTaskDetailsParams extends PaginationReqBody {
  taskRunStatus?: string;
  includeStartedOnly?: boolean;
  last24HoursOnly?: boolean;
  taskDetailsForWeekParams?: any;
}

// task details
const loadTaskDetails = (params: LoadTaskDetailsParams, dispatch: RootDispatch, showloading = true) => {
  if (showloading) {
    dispatch.monitoringDashboard.updateTaskDetails({
      loading: true,
    });
  }

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

// task chart details
const loadChartTaskDetails = (params: LoadTaskDetailsParams, dispatch: RootDispatch) => {
  dispatch.monitoringDashboard.updateTaskDetails({
    loading: true,
  });

  loadTaskDetailsFlag += 1;
  const currentFlag = loadTaskDetailsFlag;

  return services
    .fetchDataDevelopmentChartTaskDetails(params)
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
const loadStatisticChartData = async (dispatch: RootDispatch, showloading = true) => {
  let data = [];
  let error = null;
  const params = {
    timezoneOffset: 8,
  };
  if (showloading) {
    dispatch.monitoringDashboard.updateStatisticChartData({
      loading: true,
    });
  }
  const res = await services.fetchDataDevelopmentStatisticChart(params).catch(e => {
    error = e;
  });
  if (res) {
    data = res.dailyStatisticList;
  }
  dispatch.monitoringDashboard.updateStatisticChartData({
    data,
    loading: false,
    error,
  });
};
/**
 * Store effects for monitoring dashboard
 * @param dispatch
 */
export const effects = (dispatch: RootDispatch) => ({
  async reloadAll({ viewState, showloading = true }) {
    const { datasetMetrics } = viewState.dataDiscoveryBoardData;

    const { taskDetails, taskDetailsDisplayLast24HoursOnly } = viewState.dataDevelopmentBoardData;
    dispatch.monitoringDashboard.setAllSettled(false);
    Promise.allSettled([
      loadDataDiscoveryMetrics(dispatch, showloading),
      loadStatisticChartData(dispatch, showloading),
      loadTopDatasetsMaxRowCountChange(dispatch, showloading),
      loadFailedTestCases(
        {
          pageNumber: 1,
          pageSize: 65535,
        },
        dispatch,
        showloading,
      ),
      loadDatasetMetrics(
        {
          pageNumber: datasetMetrics.pageNum,
          pageSize: datasetMetrics.pageSize,
        },
        dispatch,
        showloading,
      ),
      loadDataDevelopmentMetrics(dispatch, showloading),
      loadTaskDetails(
        {
          pageNumber: taskDetails.pageNum,
          pageSize: taskDetails.pageSize,
          last24HoursOnly: taskDetailsDisplayLast24HoursOnly,
        },
        dispatch,
        showloading,
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
  async reloadFailedTestCases(params: PaginationReqBody & Partial<SortReqBody>) {
    await loadFailedTestCases(params, dispatch);
  },
  async reloadDatasetMetrics(params: PaginationReqBody) {
    await loadDatasetMetrics(params, dispatch);
  },
  async reloadDataDevelopmentMetrics() {
    await loadDataDevelopmentMetrics(dispatch);
  },
  async reloadTaskDetails(params: LoadTaskDetailsParams) {
    if (params.taskDetailsForWeekParams) {
      await loadChartTaskDetails(
        { pageNumber: params.pageNumber, pageSize: params.pageSize, ...params.taskDetailsForWeekParams },
        dispatch,
      );
    } else {
      await loadTaskDetails(params, dispatch);
    }
  },
});
