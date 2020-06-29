import { RunStatusEnum } from '@/definitions/StatEnums.type';

export interface TaskAttempt {
  attempt: number;
  endAt: string;    // ISO Date
  id: string;
  startAt: string;
  status: RunStatusEnum;
  taskId: string;
  taskName: string;
  taskRunId: string;
}
