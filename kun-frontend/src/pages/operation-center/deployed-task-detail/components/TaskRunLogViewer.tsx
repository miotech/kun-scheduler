import React, { FunctionComponent, memo, useCallback, useMemo } from 'react';
import PollingLogViewer from '@/components/PollingLogViewer';
import isNil from 'lodash/isNil';
import { fetchScheduledTaskRunLogWithoutErrorNotification } from '@/services/task-deployments/deployed-tasks';

import { TaskRun, TaskRunLog } from '@/definitions/TaskRun.type';

import styles from './TaskRunLogViewer.less';

interface OwnProps {
  taskRun: TaskRun | null;
  attempt?: number;
  onChangeAttempt?: (attempt: number) => any;
}

type Props = OwnProps;

export const TaskRunLogViewer: FunctionComponent<Props> = memo(function TaskRunLogViewer(props) {
  const { taskRun, attempt = -1, onChangeAttempt } = props;

  const onQuery = useCallback(() => {
    if (!taskRun) {
      return Promise.resolve(null);
    }
    // else
    return fetchScheduledTaskRunLogWithoutErrorNotification(taskRun.id, attempt).catch((e: any) => {
      return {
        logs: [e.response?.data?.note],
      } as TaskRunLog;
    });
  }, [taskRun, attempt]);

  const onDownload = useCallback(async () => {
    if (!taskRun) {
      return Promise.resolve([]);
    }
    const payload = await fetchScheduledTaskRunLogWithoutErrorNotification(taskRun.id, attempt, 0);
    return payload?.logs || [];
  }, [taskRun, attempt]);

  const totalAttempts = useMemo(() => {
    return Math.max(1, ...(taskRun?.attempts.map(item => item.attempt) || []));
  }, [taskRun]);

  const selectedAttempt = useMemo(() => {
    return attempt > 0 ? attempt : totalAttempts;
  }, [attempt, totalAttempts]);

  return (
    <div className={styles.LogContainer}>
      <div className={styles.InnerWrapper}>
        <PollingLogViewer
          pollInterval={3000}
          queryFn={onQuery}
          startPolling={!isNil(taskRun)}
          saveFileName={taskRun?.id}
          presetLineLimit={5000}
          onDownload={onDownload}
          totalAttempts={totalAttempts}
          selectedAttempt={selectedAttempt}
          onChangeAttempt={onChangeAttempt}
        />
      </div>
    </div>
  );
});
