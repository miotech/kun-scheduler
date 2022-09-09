import React from 'react';
import useI18n from '@/hooks/useI18n';
import c from 'clsx';
import { FinalStatus } from '@/definitions/Gantt.type';
import Styles from './TaskPopover.less';
import { GanttRenderData } from './BarRender';

interface Props {
  ganttRenderData: GanttRenderData;
  taskRunId: string;
  openDrawer: (value: string) => void;
}
export const OtherTaskPopover = (props: Props) => {
  const { ganttRenderData, taskRunId, openDrawer } = props;
  const t = useI18n();

  return (
    <div className={Styles.content}>
      <div className={c(Styles.item, Styles.wait)} >
        <div className={Styles.label}>{t('operationCenter.runningStatistics.task.waitTime')}</div>
        <div className={Styles.link} onClick={() => openDrawer(taskRunId)}>
          {ganttRenderData?.waitWidth} min
        </div>
      </div>
      {ganttRenderData.status === 'SUCCESS' && <div className={c(Styles.item, Styles.run)}>
        <div className={Styles.label}>{t('operationCenter.runningStatistics.task.runningTime')}</div>
        <div className={Styles.value}>
          {ganttRenderData?.runWidth} min
        </div>
      </div>
      }
      {ganttRenderData.status === 'FAILED' && <div className={c(Styles.item, Styles.runFailed)}>
        <div className={Styles.label}>{t('operationCenter.runningStatistics.task.runningTime')}</div>
        <div className={Styles.value}>
          {ganttRenderData?.runWidth} min
        </div>
      </div>
      }
      <div className={c(Styles.item, Styles.average)}>
        <div className={Styles.label}>{t('operationCenter.runningStatistics.averageRunningTime')}</div>
        <div className={Styles.value}>
          {ganttRenderData?.averageWidth} min
        </div>
      </div>
    </div>
  );
};


export const YarnTaskPopover = (props: Props) => {
  const { ganttRenderData, taskRunId, openDrawer } = props;
  const t = useI18n();
  const { yarnInfoObj } = ganttRenderData;
  return (
    <div className={Styles.content}>
      <div className={c(Styles.item, Styles.wait)}>
        <div className={Styles.label}>{t('operationCenter.runningStatistics.task.waitTime')}</div>
        <div className={Styles.link} onClick={() => openDrawer(taskRunId)}>
          {ganttRenderData?.waitWidth} min
        </div>
      </div>
      <div className={c(Styles.item, Styles.yarnWait)}>
        <div className={Styles.label}>{t('operationCenter.runningStatistics.task.yarnWait')}</div>
        <div className={Styles.link} onClick={() => openDrawer(taskRunId)}>
          {yarnInfoObj?.yarnWaitWidth} min
        </div>
      </div>
      {yarnInfoObj?.finalStatus === FinalStatus.SUCCEEDED &&
        <div className={c(Styles.item, Styles.yarnRun)}>
          <div className={Styles.label}>{t('operationCenter.runningStatistics.task.yarnRun')}</div>
          <div className={Styles.value}>
            {yarnInfoObj?.yarnRunWidth} min
          </div>
        </div>
      }
      {yarnInfoObj?.finalStatus === FinalStatus.FAILED &&
        <div className={c(Styles.item, Styles.yarnRunFailed)}>
          <div className={Styles.label}>{t('operationCenter.runningStatistics.task.yarnRun')}</div>
          <div className={Styles.value}>
            {yarnInfoObj?.yarnRunWidth} min
          </div>
        </div>
      }
      {ganttRenderData.status === 'SUCCESS' && <div className={c(Styles.item, Styles.overhead)}>
        <div className={Styles.label}>{t('operationCenter.runningStatistics.task.overhead')}</div>
        <div className={Styles.value}>
          {yarnInfoObj?.overheadWidth} min
        </div>
      </div>
      }
      {ganttRenderData.status === 'FAILED' && <div className={c(Styles.item, Styles.overheadFailed)}>
        <div className={Styles.label}>{t('operationCenter.runningStatistics.task.overhead')}</div>
        <div className={Styles.value}>
          {yarnInfoObj?.overheadWidth} min
        </div>
      </div>
      }
      <div className={c(Styles.item, Styles.average)}>
        <div className={Styles.label}>{t('operationCenter.runningStatistics.task.yarnAverageRunningTime')}</div>
        <div id="gantt-tooltip-averageRunTime" className={Styles.value}>
          {yarnInfoObj?.yarnAverageWidth} min
        </div>
      </div>
    </div>
  );
};
