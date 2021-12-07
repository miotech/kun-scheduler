import React, { memo } from 'react';
import { Card } from 'antd';
import Styles from './DailyTaskFinishCountTable.less'
import { dayjs } from '@/utils/datetime-utils';
import { DailyStatistic, TaskResult } from '@/services/monitoring-dashboard';
interface OwnProps {
  data: DailyStatistic[];
  loading?: boolean;
}

type Props = OwnProps;

const timeToWeek = (time: number) => {
  const day = dayjs(time as number).day()
  switch (day) {
    case 0: return 'Sun';
    case 1: return 'Mon';
    case 2: return 'Tue';
    case 3: return 'Wed';
    case 4: return 'Thu';
    case 5: return 'Fri';
    case 6: return 'Sat';
    default: return ''
  }
}

const renderAccount = (status: string, finalStatus: string, taskResultList: TaskResult[]) => {
  const res = taskResultList.find(item => (item.status === status) && (item.finalStatus === finalStatus))
  return res ? res.taskCount : ''
}
const firstColumn = ['SUCCESS', 'FAILED', 'UPSTREAM_FAILED', 'ABORTED', 'ONGOING', '', '', '', '', '', '']
const secondColumn = ['SUCCESS', 'FAILED', 'UPSTREAM_FAILED', 'ABORTED', 'ABORTED', 'FAILED', 'RUNNING', 'SUCCESS', 'UPSTREAM_FAILED', 'BLOCKED', 'CREATED']

export const DailyTaskFinishCountTable: React.FC<Props> = memo(function DailyTaskFinishCountTable(props) {
  const { data } = props;
  return (
    <Card >
      <div className={Styles.content}>
        <div className={Styles.row}>
          <div className={Styles.col}>
            <div className={Styles.column}>9点状态</div>
            {firstColumn.map((item, index) => (<div key={index} className={Styles.column}>
              {item}
            </div>))}
            <div className={Styles.column}>SUM</div>
          </div>
          <div className={Styles.col}>
            <div className={Styles.column}>最终状态</div>
            {secondColumn.map((item, index) => (<div key={index} className={Styles.column}>
              {item}
            </div>))}
          </div>
          {data.map(item => (
            <div key={item.time} className={Styles.col}>
              <div className={Styles.column}>{timeToWeek(item.time)},{dayjs(item.time as number).format('YYYY-MM-DD')}</div>
              {firstColumn.map((i, index) => (<div key={index}  className={Styles.column+' '+Styles.taskCount}>
                {renderAccount(firstColumn[index] ? firstColumn[index] : 'ONGOING', secondColumn[index], item.taskResultList)}
              </div>))}
              <div className={Styles.column+' '+Styles.taskCount}>{item.totalCount}</div>
            </div>))}
        </div>
      </div>
    </Card>
  )
})