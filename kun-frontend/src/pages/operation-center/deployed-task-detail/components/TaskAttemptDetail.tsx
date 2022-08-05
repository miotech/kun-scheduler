import React, { memo } from 'react';
import { Tabs } from 'antd';
import { FileSearchOutlined, ForkOutlined } from '@ant-design/icons';
import { TaskRunLogViewer } from '@/pages/operation-center/deployed-task-detail/components/TaskRunLogViewer';
import { TaskRunDAG } from '@/pages/operation-center/deployed-task-detail/components/TaskRunDAG';
import useI18n from '@/hooks/useI18n';
import { TaskRun } from '@/definitions/TaskRun.type';
import { Link } from 'umi';
import styles from '@/pages/operation-center/deployed-task-detail/index.less';

interface OwnProps {
  currentTab?: string;
  onTabChange?: (nextTab: string) => any;
  taskRun?: TaskRun | null;
  width?: number;
  height?: number;
  attempt?: number;
  setSelectedAttemptMap?: any;
  taskRunsData: TaskRun[];
}

type Props = OwnProps;

const TabBarExtraContent = (props: { taskRunsData: TaskRun[]; taskRun?: TaskRun | null }) => {
  const t = useI18n();
  const { taskRunsData, taskRun } = props;
  const taskRunId = taskRun ? taskRun.id : taskRunsData[0]?.id;
  const taskName = taskRun ? taskRun?.task?.name : taskRunsData[0]?.task?.name;
  if (taskRunsData.length > 0) {
    return (
      <Link
        style={{ marginRight: '15px' }}
        to={`/operation-center/running-statistics?taskRunId=${taskRunId}&taskName=${taskName}`}
      >
        {t('scheduledTasks.jumpToTaskGantt')}
      </Link>
    );
  }
  return null;
};

export const TaskAttemptDetail: React.FC<Props> = memo(function TaskAttemptDetail(props) {
  const {
    currentTab = 'logs',
    onTabChange,
    taskRun,
    width,
    height,
    attempt = -1,
    setSelectedAttemptMap,
    taskRunsData,
  } = props;
  const t = useI18n();
  const handleChangeAttempt = (nextAttempt: number) => {
    if (setSelectedAttemptMap && taskRun) {
      setSelectedAttemptMap((currentState: Record<string, number>) => {
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
      tabBarExtraContent={<TabBarExtraContent taskRunsData={taskRunsData} taskRun={taskRun} />}
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
        <div style={{ width: '100%', height: '100%', position: 'relative' }}>
          <TaskRunLogViewer taskRun={taskRun || null} attempt={attempt} onChangeAttempt={handleChangeAttempt} />
        </div>
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
