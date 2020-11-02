import React, { memo } from 'react';
import styles from '@/pages/operation-center/deployed-task-detail/index.less';
import { TaskRunDAG } from '@/pages/operation-center/deployed-task-detail/components/TaskRunDAG';
import { TaskRunLog } from '@/pages/operation-center/deployed-task-detail/components/TaskRunLog';
import { TaskRun } from '@/definitions/TaskRun.type';

interface OwnProps {
  rightPanelRef?: any;
  selectedTaskRun: TaskRun | null;
  dagContainerSize: { width?: number; height?: number };
}

type Props = OwnProps;

export const RightPanel: React.FC<Props> = memo(function RightPanel(props) {

  const {
    rightPanelRef,
    selectedTaskRun,
    dagContainerSize,
  } = props;

  return (
    <div
      className={styles.RightPanel}
      ref={rightPanelRef}
    >
      <div
        id="taskrun-dag-container"
        className={styles.DAGContainer}
      >
        <TaskRunDAG
          taskRun={selectedTaskRun}
          width={(dagContainerSize.width ?? 0)}
          height={(dagContainerSize.height ?? 0) / 2}
        />
      </div>
      <TaskRunLog
        taskRun={selectedTaskRun}
      />
    </div>
  );
});
