import React, { useEffect } from 'react';
import useI18n from '@/hooks/useI18n';
import { Drawer } from 'antd';
import { Link } from 'umi';
import { queryWaitTasks } from '@/services/gantt';
import { useRequest } from 'ahooks';
import { KunSpin } from '@/components/KunSpin';
import styles from './WaitTask.less';

interface Props {
  drawerVisible: boolean;
  setDrawerVisible: (value: boolean) => void;
  waitForTaskRunId: string;
}
export const WaitTask = (props: Props) => {
  const { drawerVisible, setDrawerVisible, waitForTaskRunId } = props;
  const t = useI18n();

  const { data: waitTasks, loading, run: doSearch } = useRequest(queryWaitTasks, {
    manual: true,
  });

  useEffect(() => {
    if (waitForTaskRunId) {
      doSearch(waitForTaskRunId);
    }
  }, [waitForTaskRunId, doSearch]);

  return (
    <Drawer
      title={t('operationCenter.runningStatistics.task.taskWaitingFor')}
      width="75%"
      placement="right"
      onClose={() => setDrawerVisible(false)}
      visible={drawerVisible}
    >
      <KunSpin spinning={loading}>
        <div className={styles.content}>
          {waitTasks?.map(task => (
            <div className={styles.task} key={task.taskRunId}>
              <div className={styles.run} style={{ width: `${parseInt(task.runningTime_seconds, 10) / 60}px` }} />
              <div className={styles.name}> {task.name} </div>
              <Link to={`/operation-center/task-run-id/${task.taskRunId}`} style={{ zIndex: 999 }} target="_blank">
                &nbsp;&nbsp;&nbsp;{t('operationCenter.runningStatistics.task.jumpToInstance')}
              </Link>
            </div>
          ))}
        </div>
      </KunSpin>
    </Drawer>
  );
};
