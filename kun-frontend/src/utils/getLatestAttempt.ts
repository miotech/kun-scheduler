import find from 'lodash/find';

import { TaskRun } from '@/definitions/TaskRun.type';
import { TaskAttempt } from '@/definitions/TaskAttempt.type';

export default function getLatestAttempt(taskRun: TaskRun): TaskAttempt | null {
  if (!taskRun.attempts.length) {
    return null;
  }
  // else
  const latestAttemptCount = Math.max(...taskRun.attempts.map(item => item.attempt));
  return find(taskRun.attempts, item => item.attempt === latestAttemptCount) || null;
}
