import React, { FunctionComponent, memo, useCallback } from 'react';
import PollingLogViewer from '@/components/PollingLogViewer';
import isNil from 'lodash/isNil';
import { fetchScheduledTaskRunLog } from '@/services/task-deployments/deployed-tasks';

import { TaskRun } from '@/definitions/TaskRun.type';

import styles from './TaskRunLog.less';

interface OwnProps {
  taskRun: TaskRun | null;
}

type Props = OwnProps;

export const TaskRunLog: FunctionComponent<Props> = memo((props) => {
  const { taskRun } = props;

  const onQuery = useCallback(() => {
    if (!taskRun) {
      return Promise.resolve(null);
    }
    // else
    return fetchScheduledTaskRunLog(taskRun.id);
  }, [
    taskRun,
  ]);

  return (
    <div className={styles.LogContainer}>
      <div className={styles.InnerWrapper}>
        <PollingLogViewer
          pollInterval={2000}
          queryFn={onQuery}
          startPolling={!isNil(taskRun)}
        />
      </div>
    </div>
  );
});
