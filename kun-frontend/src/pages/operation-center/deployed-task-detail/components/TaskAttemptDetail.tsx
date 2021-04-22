import React, { memo } from 'react';
import { Tabs } from 'antd';
import { FileSearchOutlined, ForkOutlined } from '@ant-design/icons';
import { TaskRunLogViewer } from '@/pages/operation-center/deployed-task-detail/components/TaskRunLogViewer';
import { TaskRunDAG } from '@/pages/operation-center/deployed-task-detail/components/TaskRunDAG';
import useI18n from '@/hooks/useI18n';
import { TaskRun } from '@/definitions/TaskRun.type';

import styles from '@/pages/operation-center/deployed-task-detail/index.less';

interface OwnProps {
  currentTab?: string;
  onTabChange?: (nextTab: string) => any;
  taskRun?: TaskRun | null;
  width?: number;
  height?: number;
  attempt?: number;
  setSelectedAttemptMap?: any;
}

type Props = OwnProps;

export const TaskAttemptDetail: React.FC<Props> = memo(function TaskAttemptDetail(props) {
  const { currentTab, onTabChange, taskRun, width, height, attempt = -1, setSelectedAttemptMap } = props;
  const t = useI18n();

  const handleChangeAttempt = (nextAttempt: number) => {
    if (setSelectedAttemptMap && taskRun) {
      setSelectedAttemptMap((currentState: Record<string, number>) => {
        console.log('currentState =', currentState);
        return {
          ...currentState,
          [taskRun.id]: nextAttempt,
        };
      });
    }
  };

  return (
    <Tabs
      type="card"
      defaultActiveKey="logs"
      activeKey={currentTab}
      onChange={onTabChange}
      className={styles.RightPanelTabsContainer}
    >
      <Tabs.TabPane
        tab={
          <span>
            <FileSearchOutlined />
            <span>{t('scheduledTasks.logs')}</span>
          </span>
        }
        key="logs"
      >
        <TaskRunLogViewer taskRun={taskRun || null} attempt={attempt} onChangeAttempt={handleChangeAttempt} />
      </Tabs.TabPane>
      <Tabs.TabPane
        tab={
          <span>
            <ForkOutlined />
            <span>{t('scheduledTasks.DAG')}</span>
          </span>
        }
        key="dag"
      >
        <div id="taskrun-dag-container" className={styles.DAGContainer}>
          <TaskRunDAG taskRun={taskRun || null} width={width ?? 0} height={(height ?? 20) - 20} />
        </div>
      </Tabs.TabPane>
    </Tabs>
  );
});
