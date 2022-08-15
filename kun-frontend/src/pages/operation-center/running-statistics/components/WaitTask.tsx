import React, { useEffect } from 'react';
import useI18n from '@/hooks/useI18n';
import { Drawer, Popover, Alert } from 'antd';
import { Link, useHistory } from 'umi';
import { queryWaitTasks } from '@/services/gantt';
import { useRequest } from 'ahooks';
import { KunSpin } from '@/components/KunSpin';
import { taskColorConfig } from '@/constants/colorConfig';
import styles from './WaitTask.less';

interface Props {
  drawerVisible: boolean;
  waitForTaskRunId: string;
}
const renderRunningWidth = (seconds: number) => {
  const min = Math.ceil(seconds / 60);
  return min;
};

const renderRunningTootip = (seconds: number, t: (arg: string) => string) => {
  return (
    <div className={styles.tooltip}>
      <div className={styles.label}>{t('operationCenter.runningStatistics.task.runningTime')}</div>
      <div className={styles.value}>{Math.ceil(seconds / 60)} min</div>
    </div>
  );
};
export const WaitTask = (props: Props) => {
  const { drawerVisible, waitForTaskRunId } = props;
  const t = useI18n();
  const history = useHistory();

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
      onClose={() => history.goBack()}
      visible={drawerVisible}
    >
      <KunSpin spinning={loading}>
        <div className={styles.content}>
          {waitTasks?.map(task => (
            <div className={styles.task} key={task.taskRunId}>
              <Popover content={renderRunningTootip(parseInt(task.runningTime_seconds, 10), t)}>
                <div
                  className={styles.run}
                  style={{
                    width: renderRunningWidth(parseInt(task.runningTime_seconds, 10)),
                    backgroundColor: taskColorConfig[task.status],
                  }}
                />
              </Popover>
              <div className={styles.name}> {task.name} </div>
              <Link to={`/operation-center/task-run-id/${task.taskRunId}`} style={{ zIndex: 999 }} target="_blank">
                &nbsp;&nbsp;&nbsp;{t('operationCenter.runningStatistics.task.jumpToInstance')}
              </Link>
            </div>
          ))}
        </div>
        {waitTasks?.length === 10 && (
          <Alert message={t('operationCenter.runningStatistics.task.notice')} type="warning" banner />
        )}
      </KunSpin>
    </Drawer>
  );
};
