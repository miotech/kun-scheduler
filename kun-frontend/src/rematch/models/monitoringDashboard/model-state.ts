import {
  DailyStatistic,
  DataDevelopmentMetrics,
  DevTaskDetail,
  MetadataMetrics,
  AbnormalDataset,
} from '@/services/monitoring-dashboard';

export type DataDevelopmentBoardFilterCardType = 'SUCCESS' | 'FAILED' | 'RUNNING' | 'PENDING' | 'BLOCKED' | null;

export interface DataDiscoveryBoardData {
  metadataMetrics: MetadataMetrics | null;
  metadataMetricsLoading: boolean;
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
    glossaryFilter: string | null;
    total: number;
  };
}

export interface DataDevelopmentBoardData {
  // top metrics of data development
  dataDevelopmentMetrics: DataDevelopmentMetrics;
  dataDevelopmentMetricsLoading: boolean;
  dailyStatisticList: {
    loading: boolean;
    data: DailyStatistic[];
    error: Error | null;
  };
  taskDetailsSelectedFilter: DataDevelopmentBoardFilterCardType;
  taskDetailsDisplayStartedOnly: boolean;
  taskDetailsDisplayLast24HoursOnly: boolean;
  taskDetailsForWeekParams: any;
  clearBarChart: boolean;
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
    failedTestCases: {
      loading: false,
      error: null,
      data: [],
      sortColumn: null,
      sortOrder: null,
      pageNum: 1,
      pageSize: 65535,
      showPageSize: 30,
      glossaryFilter: null,
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
    dailyStatisticList: {
      data: [],
      loading: false,
      error: null,
    },
    taskDetailsSelectedFilter: null,
    taskDetailsDisplayStartedOnly: false,
    taskDetailsDisplayLast24HoursOnly: true,
    taskDetailsForWeekParams: false,
    clearBarChart: true,
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
