import { DataStore } from '@/definitions/DataStore.type';
import { Tick } from '@/definitions/Tick.type';
import { RunStatusEnum } from '@/definitions/StatEnums.type';
import { Tag } from '@/definitions/Tag.type';
import { Task, TaskVariable } from '@/definitions/Task.type';
import { TaskAttempt } from '@/definitions/TaskAttempt.type';

export interface TaskRun {
  id: string;
  attempts: TaskAttempt[];
  dependencyTaskRunIds: string[];
  inlets: DataStore[];
  outlets: DataStore[];
  scheduledTick: Tick;
  startAt: string; // ISO Date
  endAt: string; // ISO Date
  status: RunStatusEnum;
  tags: Tag[];
  variable: TaskVariable[];
  task: Task;
}

export interface TaskRunLog {
  attempt: number;
  endLine: number;
  /* When logs == null, the task attempt exists but log file cannot be found */
  logs: string[] | null;
  startLine: number;
  taskRunId: string | number;
  isTerminated: boolean;
  status: RunStatusEnum;
}

export interface TaskRunDependency {
  downStreamTaskRunId: string;
  upstreamTaskRunId: string;
}

export interface TaskRunDAG {
  nodes: TaskRun[];
  edges: TaskRunDependency[];
}
