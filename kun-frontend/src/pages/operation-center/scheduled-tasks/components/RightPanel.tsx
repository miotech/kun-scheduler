import React, { memo, useCallback, useState } from 'react';
import { TaskRun } from '@/definitions/TaskRun.type';

import styles from '@/pages/operation-center/deployed-task-detail/index.less';
import { TaskAttemptDetail } from '@/pages/operation-center/deployed-task-detail/components/TaskAttemptDetail';

interface OwnProps {
  rightPanelRef?: any;
  selectedTaskRun: TaskRun | null;
  dagContainerSize: { width?: number; height?: number };
  selectedAttemptMap: Record<string, number>;
  setSelectedAttemptMap: (nextState: Record<string, number>) => any;
}

type Props = OwnProps;

export const RightPanel: React.FC<Props> = memo(function RightPanel(props) {
  const { rightPanelRef, selectedTaskRun, dagContainerSize, selectedAttemptMap, setSelectedAttemptMap } = props;

  const [currentTab, setCurrentTab] = useState<string>('logs');

  const handleTabChange = useCallback((activeKey: string) => {
    setCurrentTab(activeKey);
  }, []);

  return (
    <div className={styles.RightPanel} ref={rightPanelRef}>
      <TaskAttemptDetail
        currentTab={currentTab}
        onTabChange={handleTabChange}
        taskRun={selectedTaskRun}
        width={dagContainerSize?.width}
        height={dagContainerSize?.height}
        attempt={
          selectedTaskRun && selectedAttemptMap[selectedTaskRun.id] ? selectedAttemptMap[selectedTaskRun.id] : undefined
        }
        setSelectedAttemptMap={setSelectedAttemptMap}
      />
    </div>
  );
});
