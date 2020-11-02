import React, { memo, useCallback, useState } from 'react';
import styles from '@/pages/operation-center/deployed-task-detail/index.less';
import { TaskRunDAG } from '@/pages/operation-center/deployed-task-detail/components/TaskRunDAG';
import { TaskRunLog } from '@/pages/operation-center/deployed-task-detail/components/TaskRunLog';
import { TaskRun } from '@/definitions/TaskRun.type';
import { Tabs } from 'antd';

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

  const [ currentTab, setCurrentTab ] = useState<string>('logs');

  const handleTabChange = useCallback((activeKey: string) => {
    setCurrentTab(activeKey);
  }, []);

  return (
    <div
      className={styles.RightPanel}
      ref={rightPanelRef}
    >
      <Tabs
        type="card"
        defaultActiveKey="logs"
        activeKey={currentTab}
        onChange={handleTabChange}
        className={styles.RightPanelTabsContainer}
      >
        <Tabs.TabPane tab="Logs" key="logs">
          <TaskRunLog
            taskRun={selectedTaskRun}
          />
        </Tabs.TabPane>
        <Tabs.TabPane tab="DAG" key="dag">
          <div
            id="taskrun-dag-container"
            className={styles.DAGContainer}
          >
            <TaskRunDAG
              taskRun={selectedTaskRun}
              width={(dagContainerSize.width ?? 0)}
              height={(dagContainerSize.height ?? 20) - 20}
            />
          </div>
        </Tabs.TabPane>
      </Tabs>
    </div>
  );
});
