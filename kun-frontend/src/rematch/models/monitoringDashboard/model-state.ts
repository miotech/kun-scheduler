import {
  ColumnMetrics, DailyTaskCount,
  DataDevelopmentMetrics, DevTaskDetail,
  FailedTestCase,
  MetadataMetrics,
  RowCountChange
} from '@/services/monitoring-dashboard';

export interface DataDiscoveryBoardData {
  metadataMetrics: MetadataMetrics | null;
  // Top 10 Datasets with Max Row Count Change table state
  maxRowCountChange: {
    error: Error | null;
    data: RowCountChange[];
  };
  // failed test cases table state
  failedTestCases: {
    error: Error | null;
    data: FailedTestCase[];
    sortColumn: string | null;
    sortOrder: 'ASC' | 'DESC' | null;
    pageNum: number;
    pageSize: number;
    total: number;
  };
  // dataset metrics table state
  datasetMetrics: {
    error: Error | null;
    data: ColumnMetrics[];
    pageNum: number;
    pageSize: number;
    total: number;
  };
}

export interface DataDevelopmentBoardData {
  // top metrics of data development
  dataDevelopmentMetrics: DataDevelopmentMetrics;
  dailyTaskFinish: {
    data: DailyTaskCount[];
    error: Error | null;
  };
  taskDetails: {
    data: DevTaskDetail[];
    error: Error | null;
    pageNum: number;
    pageSize: number;
    total: number;
  };
}

export interface MonitoringDashboardModelState {
  allSettled: boolean;
  dataDiscoveryBoardData: DataDiscoveryBoardData;
  dataDevelopmentBoardData: DataDevelopmentBoardData;
}

export const initState: MonitoringDashboardModelState = {
  allSettled: false,
  dataDiscoveryBoardData: {
    metadataMetrics: null,
    maxRowCountChange: {
      error: null,
      data: [],
    },
    failedTestCases: {
      error: null,
      data: [],
      sortColumn: null,
      sortOrder: null,
      pageNum: 1,
      pageSize: 10,
      total: 0,
    },
    datasetMetrics: {
      error: null,
      data: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
    },
  },
  dataDevelopmentBoardData: {
    dataDevelopmentMetrics: {
      successTaskCount: 0,
      failedTaskCount: 0,
      runningTaskCount: 0,
      totalTaskCount: 0,
    },
    dailyTaskFinish: {
      data: [],
      error: null,
    },
    taskDetails: {
      data: [],
      error: null,
      pageNum: 1,
      pageSize: 15,
      total: 0,
    },
  },
};
