import React, { memo, useCallback } from 'react';
import { history } from 'umi';
import { TaskRun } from '@/definitions/TaskRun.type';

import styles from '@/pages/operation-center/deployed-task-detail/index.less';
import { TaskAttemptDetail } from '@/pages/operation-center/deployed-task-detail/components/TaskAttemptDetail';
import SafeUrlAssembler from 'safe-url-assembler';

interface OwnProps {
  rightPanelRef?: any;
  selectedTaskRun: TaskRun | null;
  dagContainerSize: { width?: number; height?: number };
  selectedAttemptMap: Record<string, number>;
  setSelectedAttemptMap: (nextState: Record<string, number>) => any;
  currentTab?: string;
  setCurrentTab?: (nextActiveTab: string) => any;
}

type Props = OwnProps;

export const RightPanel: React.FC<Props> = memo(function RightPanel(props) {
  const {
    rightPanelRef,
    selectedTaskRun,
    dagContainerSize,
    selectedAttemptMap,
    setSelectedAttemptMap,
    currentTab = 'logs',
    setCurrentTab,
  } = props;

  const handleTabChange = useCallback(
    (activeKey: string) => {
      if (setCurrentTab) {
        setCurrentTab(activeKey);
      }
      history.push(
        SafeUrlAssembler()
          .template(window.location.pathname)
          .query({
            taskRunId: selectedTaskRun?.id,
            tab: activeKey,
          })
          .toString(),
      );
    },
    [selectedTaskRun?.id, setCurrentTab],
  );

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
