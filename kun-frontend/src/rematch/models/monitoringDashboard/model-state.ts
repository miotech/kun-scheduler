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
      pageSize: 10,
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
      data: [
        {
            "time": 1638493200000,
            "totalCount": 50,
            "taskResultList": [
                {
                    "status": "SUCCESS",
                    "finalStatus": "SUCCESS",
                    "taskCount": 44
                },
                {
                    "status": "FAILED",
                    "finalStatus": "FAILED",
                    "taskCount": 2
                },
                {
                    "status": "UPSTREAM_FAILED",
                    "finalStatus": "UPSTREAM_FAILED",
                    "taskCount": 0
                },
                {
                    "status": "ABORTED",
                    "finalStatus": "ABORTED",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "FAILED",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "UPSTREAM_FAILED",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "RUNNING",
                    "taskCount": 3
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "SUCCESS",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "ABORTED",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "CREATED",
                    "taskCount": 1
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "BLOCKED",
                    "taskCount": 0
                }
            ]
        },
        {
            "time": 1638406800000,
            "totalCount": 154,
            "taskResultList": [
                {
                    "status": "SUCCESS",
                    "finalStatus": "SUCCESS",
                    "taskCount": 148
                },
                {
                    "status": "FAILED",
                    "finalStatus": "FAILED",
                    "taskCount": 3
                },
                {
                    "status": "UPSTREAM_FAILED",
                    "finalStatus": "UPSTREAM_FAILED",
                    "taskCount": 1
                },
                {
                    "status": "ABORTED",
                    "finalStatus": "ABORTED",
                    "taskCount": 20
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "FAILED",
                    "taskCount": 10
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "UPSTREAM_FAILED",
                    "taskCount": 31
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "RUNNING",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "SUCCESS",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "ABORTED",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "CREATED",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "BLOCKED",
                    "taskCount": 0
                }
            ]
        },
        {
            "time": 1638320400000,
            "totalCount": 162,
            "taskResultList": [
                {
                    "status": "SUCCESS",
                    "finalStatus": "SUCCESS",
                    "taskCount": 154
                },
                {
                    "status": "FAILED",
                    "finalStatus": "FAILED",
                    "taskCount": 3
                },
                {
                    "status": "UPSTREAM_FAILED",
                    "finalStatus": "UPSTREAM_FAILED",
                    "taskCount": 0
                },
                {
                    "status": "ABORTED",
                    "finalStatus": "ABORTED",
                    "taskCount": 5
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "FAILED",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "UPSTREAM_FAILED",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "RUNNING",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "SUCCESS",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "ABORTED",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "CREATED",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "BLOCKED",
                    "taskCount": 0
                }
            ]
        },
        {
            "time": 1638234000000,
            "totalCount": 173,
            "taskResultList": [
                {
                    "status": "SUCCESS",
                    "finalStatus": "SUCCESS",
                    "taskCount": 170
                },
                {
                    "status": "FAILED",
                    "finalStatus": "FAILED",
                    "taskCount": 3
                },
                {
                    "status": "UPSTREAM_FAILED",
                    "finalStatus": "UPSTREAM_FAILED",
                    "taskCount": 0
                },
                {
                    "status": "ABORTED",
                    "finalStatus": "ABORTED",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "FAILED",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "UPSTREAM_FAILED",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "RUNNING",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "SUCCESS",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "ABORTED",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "CREATED",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "BLOCKED",
                    "taskCount": 0
                }
            ]
        },
        {
            "time": 1638147600000,
            "totalCount": 173,
            "taskResultList": [
                {
                    "status": "SUCCESS",
                    "finalStatus": "SUCCESS",
                    "taskCount": 170
                },
                {
                    "status": "FAILED",
                    "finalStatus": "FAILED",
                    "taskCount": 3
                },
                {
                    "status": "UPSTREAM_FAILED",
                    "finalStatus": "UPSTREAM_FAILED",
                    "taskCount": 0
                },
                {
                    "status": "ABORTED",
                    "finalStatus": "ABORTED",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "FAILED",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "UPSTREAM_FAILED",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "RUNNING",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "SUCCESS",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "ABORTED",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "CREATED",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "BLOCKED",
                    "taskCount": 0
                }
            ]
        },
        {
            "time": 1638061200000,
            "totalCount": 173,
            "taskResultList": [
                {
                    "status": "SUCCESS",
                    "finalStatus": "SUCCESS",
                    "taskCount": 170
                },
                {
                    "status": "FAILED",
                    "finalStatus": "FAILED",
                    "taskCount": 3
                },
                {
                    "status": "UPSTREAM_FAILED",
                    "finalStatus": "UPSTREAM_FAILED",
                    "taskCount": 0
                },
                {
                    "status": "ABORTED",
                    "finalStatus": "ABORTED",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "FAILED",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "UPSTREAM_FAILED",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "RUNNING",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "SUCCESS",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "ABORTED",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "CREATED",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "BLOCKED",
                    "taskCount": 0
                }
            ]
        },
        {
            "time": 1637974800000,
            "totalCount": 173,
            "taskResultList": [
                {
                    "status": "SUCCESS",
                    "finalStatus": "SUCCESS",
                    "taskCount": 170
                },
                {
                    "status": "FAILED",
                    "finalStatus": "FAILED",
                    "taskCount": 3
                },
                {
                    "status": "UPSTREAM_FAILED",
                    "finalStatus": "UPSTREAM_FAILED",
                    "taskCount": 0
                },
                {
                    "status": "ABORTED",
                    "finalStatus": "ABORTED",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "FAILED",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "UPSTREAM_FAILED",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "RUNNING",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "SUCCESS",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "ABORTED",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "CREATED",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "BLOCKED",
                    "taskCount": 0
                }
            ]
        },
        {
            "time": 1637888400000,
            "totalCount": 173,
            "taskResultList": [
                {
                    "status": "SUCCESS",
                    "finalStatus": "SUCCESS",
                    "taskCount": 170
                },
                {
                    "status": "FAILED",
                    "finalStatus": "FAILED",
                    "taskCount": 3
                },
                {
                    "status": "UPSTREAM_FAILED",
                    "finalStatus": "UPSTREAM_FAILED",
                    "taskCount": 0
                },
                {
                    "status": "ABORTED",
                    "finalStatus": "ABORTED",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "FAILED",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "UPSTREAM_FAILED",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "RUNNING",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "SUCCESS",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "ABORTED",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "CREATED",
                    "taskCount": 0
                },
                {
                    "status": "ONGOING",
                    "finalStatus": "BLOCKED",
                    "taskCount": 0
                }
            ]
      }],
      loading: false,
      error: null
    },
    taskDetailsSelectedFilter: null,
    taskDetailsDisplayStartedOnly: false,
    taskDetailsDisplayLast24HoursOnly: true,
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
