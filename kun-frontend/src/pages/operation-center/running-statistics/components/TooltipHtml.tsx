import React from 'react';
import useI18n from '@/hooks/useI18n';
import Styles from './TooltipHtml.less';

interface Props {
  waitTime: number;
  runTime: number;
  averageRunTime: number;
  taskRunId: string;
  openDrawer: (value: string) => void;
}
export const TooltipHtml = (props: Props) => {
  const { waitTime, runTime, averageRunTime, taskRunId, openDrawer } = props;
  const t = useI18n();

  return (
    <div className={Styles.content}>
      <div className={Styles.item}>
        <div className={Styles.label}>{t('operationCenter.runningStatistics.task.waitTime')}</div>
        <div id="gantt-tooltip-waitTime" className={Styles.link} onClick={() => openDrawer(taskRunId)}>
          {waitTime} min
        </div>
      </div>
      <div className={Styles.item}>
        <div className={Styles.label}>{t('operationCenter.runningStatistics.task.runningTime')}</div>
        <div id="gantt-tooltip-runTime" className={Styles.value}>
          {runTime} min
        </div>
      </div>
      <div className={Styles.item}>
        <div className={Styles.label}>{t('operationCenter.runningStatistics.averageRunningTime')}</div>
        <div id="gantt-tooltip-averageRunTime" className={Styles.value}>
          {averageRunTime} min
        </div>
      </div>
    </div>
  );
};
