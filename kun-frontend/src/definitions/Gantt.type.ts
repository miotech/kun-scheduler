export enum ResourceManagementPlatform {
  'YARN' = 'YARN',
  "OTHER" = "OTHER"
}

export enum FinalStatus {
  'SUCCEEDED' = 'SUCCEEDED',
  'FAILED' = 'FAILED',
}

export interface YarnInfo {
  startAt: Date;
  runningAt: Date;
  endAt: Date;
  averageRunningTime: string;
  finalStatus: FinalStatus
}
export interface Task {
  taskRunId: string;
  taskId: string;
  name: string;
  status: string;
  queuedAt: Date;
  startAt: Date;
  endAt: Date;
  createdAt: Date;
  averageRunningTime: string;
  averageQueuingTime: string;
  dependentTaskRunIds: string[];
  resourceManagementPlatform: ResourceManagementPlatform;
  yarnInfo?: YarnInfo;
}
export interface Tasks {
  infoList: Task[];
  earliestTime: string;
  latestTime: string;
}

// export interface GanttData extends Task {

// }
export interface QueryGanttTasksParams {
  startTime?: string | null;
  endTime?: string | null;
  timeType?: string | null;
  taskRunId?: string | null;
}

export interface Filters {
  startTime?: string | null;
  endTime?: string | null;
  timeType?: string | null;
  taskRunId?: string | null;
  name?: string | null;
}

export interface WaitTask {
  taskRunId: string;
  name: string;
  status: string;
  runningTime_seconds: string;
}
