import React, { memo } from 'react';
import Styles from './TooltipHtml.less';

interface Props {
  t: (params: string) => {};
}
export const TooltipHtml = memo((props: Props) => {
  const { t } = props;
  return (
    <div className={Styles.content}>
      <div className={Styles.item}>
        <div className={Styles.label}>{t('operationCenter.runningStatistics.task.waitTime')}</div>
        <div id="gantt-tooltip-waitTime" className={Styles.value}>
          {' '}
        </div>
      </div>
      <div className={Styles.item}>
        <div className={Styles.label}>{t('operationCenter.runningStatistics.task.runningTime')}</div>
        <div id="gantt-tooltip-runTime" className={Styles.value}>
          {' '}
        </div>
      </div>
      <div className={Styles.item}>
        <div className={Styles.label}>{t('operationCenter.runningStatistics.averageRunningTime')}</div>
        <div id="gantt-tooltip-averageRunTime" className={Styles.value}>
          {' '}
        </div>
      </div>
    </div>
  );
});
