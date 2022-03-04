export interface Task {
  taskRunId: string;
  taskId: string;
  name: string;
  status: string;
  queuedAt: string;
  startAt: string;
  endAt: string;
  createdAt: string;
  averageRunningTime: string;
  averageQueuingTime: string;
  dependentTaskRunIds: string[];
}
export interface Tasks {
  infoList: Task[];
  earliestTime: string;
  latestTime: string;
}

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

export interface TaskState {
  taskName: string;
  taskRunId: string;
}
