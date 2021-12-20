import {
  ColumnMetrics,
  DailyTaskCount,
  DailyStatistic,
  DataDevelopmentMetrics,
  DevTaskDetail,
  MetadataMetrics,
  RowCountChange,
  AbnormalDataset,
} from '@/services/monitoring-dashboard';

export type DataDevelopmentBoardFilterCardType = 'SUCCESS' | 'FAILED' | 'RUNNING' | 'PENDING' | 'BLOCKED' | null;

export interface DataDiscoveryBoardData {
  metadataMetrics: MetadataMetrics | null;
  metadataMetricsLoading: boolean;
  // Top 10 Datasets with Max Row Count Change table state
  maxRowCountChange: {
    loading: boolean;
    error: Error | null;
    data: RowCountChange[];
  };
  // failed test cases table state
  failedTestCases: {
    loading: boolean;
    error: Error | null;
    data: AbnormalDataset[];
    sortColumn: string | null;
    sortOrder: 'ASC' | 'DESC' | null;
    pageNum: number;
    pageSize: number;
    showPageSize: number;
    total: number;
  };
  // dataset metrics table state
  datasetMetrics: {
    loading: boolean;
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
  dataDevelopmentMetricsLoading: boolean;
  dailyTaskFinish: {
    loading: boolean;
    data: DailyTaskCount[];
    error: Error | null;
  };
  dailyStatisticList: {
    loading: boolean;
    data: DailyStatistic[];
    error: Error | null;
  };
  taskDetailsSelectedFilter: DataDevelopmentBoardFilterCardType;
  taskDetailsDisplayStartedOnly: boolean;
  taskDetailsDisplayLast24HoursOnly: boolean;
  taskDetailsForWeekParams: any;
  taskDetails: {
    data: DevTaskDetail[];
    loading: boolean;
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
    metadataMetricsLoading: false,
    maxRowCountChange: {
      loading: false,
      error: null,
      data: [],
    },
    failedTestCases: {
      loading: false,
      error: null,
      data: [],
      sortColumn: null,
      sortOrder: null,
      pageNum: 1,
      pageSize: 65535,
      showPageSize: 10,
      total: 0,
    },
    datasetMetrics: {
      loading: false,
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
      pendingTaskCount: 0,
      startedTaskCount: 0,
      upstreamFailedTaskCount: 0,
    },
    dataDevelopmentMetricsLoading: false,
    dailyTaskFinish: {
      data: [],
      loading: false,
      error: null,
    },
    dailyStatisticList: {
      data: [],
      loading: false,
      error: null,
    },
    taskDetailsSelectedFilter: null,
    taskDetailsDisplayStartedOnly: false,
    taskDetailsDisplayLast24HoursOnly: true,
    taskDetailsForWeekParams: false,
    taskDetails: {
      data: [],
      loading: false,
      error: null,
      pageNum: 1,
      pageSize: 15,
      total: 0,
    },
  },
};
