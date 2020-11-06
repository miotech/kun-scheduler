import React, { memo, useCallback, useState } from 'react';
import { Tabs } from 'antd';
import useI18n from '@/hooks/useI18n';
import { FileSearchOutlined, ForkOutlined } from '@ant-design/icons';
import { TaskRunDAG } from '@/pages/operation-center/deployed-task-detail/components/TaskRunDAG';
import { TaskRunLog } from '@/pages/operation-center/deployed-task-detail/components/TaskRunLog';
import { TaskRun } from '@/definitions/TaskRun.type';

import styles from '@/pages/operation-center/deployed-task-detail/index.less';

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

  const t = useI18n();

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
        <Tabs.TabPane tab={<span>
          <FileSearchOutlined />
          <span>{t('scheduledTasks.logs')}</span>
        </span>} key="logs">
          <TaskRunLog
            taskRun={selectedTaskRun}
          />
        </Tabs.TabPane>
        <Tabs.TabPane tab={
          <span>
            <ForkOutlined />
            <span>{t('scheduledTasks.DAG')}</span>
          </span>
        } key="dag">
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
