import React, { memo } from 'react';
import { DailyStatistic, TaskResult } from '@/services/monitoring-dashboard';
import c from 'clsx';
import useRedux from '@/hooks/useRedux';
import useI18n from '@/hooks/useI18n';
import Styles from './DailyTaskFinishCountTable.less';

interface OwnProps {
  data: DailyStatistic[];
  loading?: boolean;
}

type Props = OwnProps;


const renderCount = (ischecked: boolean, status: string, finalStatus: string, taskResultList: TaskResult[]) => {
  if (!status) {
    status = 'ONGOING';
  }
  const res = taskResultList.find(item => item.status === status && item.finalStatus === finalStatus);
  let count = res ? res.taskCount : 0;
  if (finalStatus === 'FAILED' || finalStatus === 'CHECK_FAILED') {
    let color = '';
    if (count > 0 && count <= 20) {
      color = '#ffe0d7';
    } else if (count > 20 && count <= 40) {
      color = '#ffc1af';
    } else if (count > 40 && count <= 60) {
      color = '#ffa286';
    } else if (count > 60 && count <= 80) {
      color = '#ff825e';
    } else if (count > 80) {
      color = '#ff6336';
    }
    return <div style={{ backgroundColor: ischecked ? '' : color }} className={Styles.countForErr} >{count}</div>;
  }
  if (finalStatus === 'TOTAL') {
    count = taskResultList.filter(item => item.status === 'ONGOING').reduce((accumulator, currentValue) => (accumulator + currentValue.taskCount),0);
  }
  return count;
};

const isChecked = (showId: string, time: string, status: string, finalStatus: string) => {
  if (!status) {
    status = 'ONGOING';
  }
  if (showId === (time + status + finalStatus)) {
    return true;
  }
  return false;
};

const firstColumn = ['SUCCESS', 'FAILED',  'CHECK_FAILED', 'UPSTREAM_FAILED', 'ABORTED', 'ONGOING', '', '','', '', '', '', '', '', ''];
const secondColumn = [
  'SUCCESS',
  'FAILED',
  'CHECK_FAILED',
  'UPSTREAM_FAILED',
  'ABORTED',
  'ABORTED',
  'FAILED',
  'RUNNING',
  'SUCCESS',
  'CHECK_FAILED',
  'UPSTREAM_FAILED',
  'QUEUED',
  'BLOCKED',
  'CREATED',
  'TOTAL',
  '',
];

export const DailyTaskFinishCountTable: React.FC<Props> = memo(function DailyTaskFinishCountTable(props) {
  const { data } = props;
  const t = useI18n();
  const {
    selector: {
      taskDetailsForWeekParams
    },
    dispatch,
  } = useRedux(s => ({
    taskDetailsForWeekParams: s.monitoringDashboard.dataDevelopmentBoardData.taskDetailsForWeekParams,
  }));
  const setParams = (targetTime: string, status: string, finalStatus: string) => {
    if(finalStatus === 'TOTAL') {
      return;
    }
    if (!status) {
      status = 'ONGOING';
    }
    const params = {
      targetTime,
      status,
      finalStatus,
      timezoneOffset: 8,
      showId: targetTime + status + finalStatus
    };
    dispatch.monitoringDashboard.setTaskDetailsForWeekParams(params);
  };
  return (
    <div className={Styles.content}>
      <div className={Styles.row}>
        <div className={Styles.col} style={{ maxWidth: '55px' }}>
          <div className={Styles.columnLeft}>
            {t('monitoringDashboard.dataDevelopment.dailyTaskFinishCountChart.status')}
          </div>
          {firstColumn.map((item, index) => (
            <div key={item + index} className={Styles.columnLeft}>
              {item}
            </div>
          ))}
          <div className={Styles.columnLeft}>SUM</div>
        </div>
        <div className={Styles.col} style={{ maxWidth: '55px' }}>
          <div className={Styles.columnLeft}>
            {t('monitoringDashboard.dataDevelopment.dailyTaskFinishCountChart.finallyStatus')}
          </div>
          {secondColumn.map((item, index) => (
            <div key={item + index} className={Styles.columnLeft}>
              {item}
            </div>
          ))}
        </div>
      </div>
      <div className={Styles.row} style={{ justifyContent: 'space-around', minWidth: '676px' }}>
        {data && data.map(item => (
          <div key={item.time} className={Styles.col}>
            <div className={Styles.column} />
            {firstColumn.map((i, index) => (
              <div
                key={i + index}
                onClick={() =>
                  setParams(item.time, firstColumn[index], secondColumn[index])
                }
                className={c(Styles.column, Styles.taskCount, taskDetailsForWeekParams && isChecked(taskDetailsForWeekParams.showId, item.time, firstColumn[index], secondColumn[index]) ? Styles.checked : '')}
              >
                {renderCount(
                  taskDetailsForWeekParams && isChecked(taskDetailsForWeekParams.showId, item.time, firstColumn[index], secondColumn[index]),
                  firstColumn[index],
                  secondColumn[index],
                  item.taskResultList,
                )}
              </div>
            ))}
            <div className={c(Styles.column, Styles.taskCount)} style={{ cursor: 'revert' }}>
              {item.totalCount}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
});
