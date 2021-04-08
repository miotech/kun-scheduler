import React, { FunctionComponent, memo, useCallback } from 'react';
import PollingLogViewer from '@/components/PollingLogViewer';
import isNil from 'lodash/isNil';
import { fetchScheduledTaskRunLogWithoutErrorNotification } from '@/services/task-deployments/deployed-tasks';

import { TaskRun, TaskRunLog } from '@/definitions/TaskRun.type';

import styles from './TaskRunLogViewer.less';

interface OwnProps {
  taskRun: TaskRun | null;
}

type Props = OwnProps;

export const TaskRunLogViewer: FunctionComponent<Props> = memo(function TaskRunLogViewer(props) {
  const { taskRun } = props;

  const onQuery = useCallback(() => {
    if (!taskRun) {
      return Promise.resolve(null);
    }
    // else
    return fetchScheduledTaskRunLogWithoutErrorNotification(taskRun.id).catch(e => {
      return {
        logs: [e.response?.data?.note],
      } as TaskRunLog;
    });
  }, [taskRun]);

  const onDownload = useCallback(async () => {
    if (!taskRun) {
      return Promise.resolve([]);
    }
    const payload = await fetchScheduledTaskRunLogWithoutErrorNotification(taskRun.id, 0);
    return payload?.logs || [];
  }, [taskRun]);

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
        />
      </div>
    </div>
  );
});
