
import React, { memo, useMemo, useCallback } from 'react';
import { Task, YarnInfo, ResourceManagementPlatform, FinalStatus } from '@/definitions/Gantt.type';
import moment from 'moment';
import { Popover } from 'antd';
import { Link } from 'umi';
import useI18n from '@/hooks/useI18n';
import useUrlState from '@ahooksjs/use-url-state';
import Highlighter from "react-highlight-words";
import { OtherTaskPopover, YarnTaskPopover } from './TaskPophover';
import style from './BarRender.less';

// 求时间中间隔多少m
function getIntervalMinutes(startDate: Date, endDate: Date) {
  const ms = moment(endDate).valueOf() - moment(startDate).valueOf();
  if (ms < 0) return 0;
  return Math.ceil(ms / 1000 / 60);
}


interface BarRendererProps {
  task: Task,
  toolBarStartTime: Date,
  taskRunId: string | null,
  top: number,
  searchWords: string,
  isSelected: boolean,
}

interface YarnInfoObj extends YarnInfo {
  yarnWaitWidth: number,
  yarnRunWidth: number,
  overheadWidth: number,
  yarnAverageWidth: number,
  translateX: number,
}
export interface GanttRenderData {
  waitLeft: number,
  waitWidth: number,
  runWidth: number,
  endAt: Date,
  status: string,
  taskRunId: string,
  taskId: string,
  averageWidth: number,
  name: string,
  resourceManagementPlatform: ResourceManagementPlatform,
  yarnInfoObj: YarnInfoObj
}

interface TaskRenderProps {
  ganttRenderData: GanttRenderData
}

const YarnTaskBar: React.FC<TaskRenderProps> = memo(function YarnTaskBar(props) {
  const { ganttRenderData } = props;
  const { yarnInfoObj } = ganttRenderData;
  return (
    <>
      <div className={style.yarnWait} style={{ width: `${yarnInfoObj?.yarnWaitWidth}px` }} />
      {yarnInfoObj.finalStatus === FinalStatus.SUCCEEDED &&
        <div
          className={style.yarnRun}
          style={{
            width: `${yarnInfoObj.yarnRunWidth}px`,
          }}
        />
      }
      {yarnInfoObj.finalStatus === FinalStatus.FAILED &&
        <div
          className={style.yarnRunFailed}
          style={{
            width: `${yarnInfoObj.yarnRunWidth}px`,
          }}
        />
      }

      {ganttRenderData.status === 'SUCCESS' &&
        <div className={style.overhead} style={{ width: `${yarnInfoObj.overheadWidth}px` }} />
      }

      {ganttRenderData.status === 'FAILED' &&
        <div className={style.overheadFailed} style={{ width: `${yarnInfoObj.overheadWidth}px` }} />
      }

      <div
        className={style.average}
        style={{ width: `${yarnInfoObj.yarnAverageWidth}px`, transform: `translateX(${yarnInfoObj.translateX}px)` }}
      />
    </>
  );
});

const OtherTaskBar: React.FC<TaskRenderProps> = memo(function OtherTaskBar(props) {
  const { ganttRenderData } = props;
  return (
    <>
      {ganttRenderData.status === 'SUCCESS' && (
        <div className={style.barRun} style={{ width: `${ganttRenderData.runWidth}px` }} />
      )}
      {ganttRenderData.status === 'FAILED' && (
        <div
          className={style.barRunFailed}
          style={{
            width: `${ganttRenderData.runWidth}px`,
          }}
        />
      )}

      <div
        className={style.average}
        style={{ width: `${ganttRenderData.averageWidth}px`, transform: `translateX(${ganttRenderData.waitWidth}px)` }}
      />
    </>
  );
});

export const BarRender = memo((props: BarRendererProps) => {
  const { task, toolBarStartTime, taskRunId, top, searchWords, isSelected } = props;
  const t = useI18n();
  const [routeState, setRouteState] = useUrlState({});

  const ganttRenderData = useMemo(() => {
    const { yarnInfo } = task;
    const waitLeft = task.queuedAt ? getIntervalMinutes(toolBarStartTime, task.queuedAt) || 1 : 0;
    const waitWidth = task.queuedAt && task.startAt ? getIntervalMinutes(task.queuedAt, task.startAt) || 1 : 0;
    const runWidth = task.startAt ? getIntervalMinutes(task.startAt, task.endAt) || 1 : 0;
    let yarnInfoObj = null;
    if (yarnInfo) {
      const yarnWaitWidth = yarnInfo?.runningAt && yarnInfo?.startAt ? getIntervalMinutes(yarnInfo.startAt, yarnInfo.runningAt) || 1 : 0;
      const yarnRunWidth = yarnInfo?.startAt && yarnInfo?.endAt ? getIntervalMinutes(yarnInfo.runningAt, yarnInfo.endAt) || 1 : 0;
      const overheadWidth = task?.endAt && yarnInfo?.endAt ? getIntervalMinutes(yarnInfo.endAt, task?.endAt) || 1 : 0;
      const yarnAverageWidth = yarnInfo?.averageRunningTime ? Math.ceil(parseInt(yarnInfo.averageRunningTime, 10) / 60) : 0;
      yarnInfoObj = {
        yarnWaitWidth,
        yarnRunWidth,
        overheadWidth,
        yarnAverageWidth,
        translateX: waitWidth + yarnWaitWidth,
        ...yarnInfo
      };
    }
    return {
      waitLeft,
      waitWidth,
      runWidth,
      endAt: task.endAt,
      status: task.status,
      taskRunId: task.taskRunId,
      taskId: task.taskId,
      averageWidth: task.averageRunningTime ? Math.ceil(parseInt(task.averageRunningTime, 10) / 60) : 0,
      name: task.name,
      resourceManagementPlatform: task.resourceManagementPlatform,
      yarnInfoObj,
    } as GanttRenderData;
  }, [task, toolBarStartTime]);

  const onMouseMove = useCallback(
    e => {
      const left = e.clientX - 86;
      const dataId = e.target.getAttribute('data-id');
      document.getElementById('line').style.transform = `translateX(${left}px)`;
      document.getElementById('line').style.display = 'block';

      document.getElementById('currentTime').style.transform = `translateX(${left - 50}px)`;
      document.getElementById('currentTime').style.display = 'block';

      const timeSize = left + document.getElementById('toolbar').scrollLeft;
      document.getElementById('currentTime').innerHTML = moment(toolBarStartTime)
        .add(timeSize, 'm')
        .format('MM-DD, HH:mm');
    },
    [toolBarStartTime],
  );


  const openDrawer = useCallback(
    (id: string) => {
      setRouteState({
        waitForTaskRunId: id,
      });
    },
    [setRouteState],
  );

  return (
    <div className={style.barItem} style={{ top: `${top}px` }} key={task.taskRunId}>
      <Popover
        content={
          <>
            {ganttRenderData.resourceManagementPlatform === ResourceManagementPlatform.OTHER &&
              <OtherTaskPopover
                ganttRenderData={ganttRenderData}
                taskRunId={task.taskRunId}
                openDrawer={openDrawer}
              />}
            {ganttRenderData.resourceManagementPlatform === ResourceManagementPlatform.YARN &&
              <YarnTaskPopover
                ganttRenderData={ganttRenderData}
                taskRunId={task.taskRunId}
                openDrawer={openDrawer}
              />
            }
          </>
        }
        mouseEnterDelay={0.3}
        placement="topLeft"
        destroyTooltipOnHide
        trigger="hover"
      >
        <div
          key={task.taskRunId}
          className={style.barCon}
          style={{ transform: `translateX(${ganttRenderData.waitLeft}px)` }}
        >
          <div onMouseMove={e => onMouseMove(e)} style={{ display: 'flex', alignItems: 'center' }}>
            <div
              className={style.barWait}
              onClick={() => openDrawer(ganttRenderData.taskRunId)}
              style={{ width: `${ganttRenderData.waitWidth}px` }}
              data-id="barWait"
            />
            {task.resourceManagementPlatform === ResourceManagementPlatform.YARN && task?.yarnInfo && (
              <YarnTaskBar ganttRenderData={ganttRenderData} />
            )}
            {task.resourceManagementPlatform === ResourceManagementPlatform.OTHER && (
              <OtherTaskBar ganttRenderData={ganttRenderData} />
            )}
          </div>
          <Link
            to={`/operation-center/running-statistics?taskRunId=${ganttRenderData.taskRunId}&taskName=${ganttRenderData.name}`}
            className={style.name}
            style={{ color: ganttRenderData.taskRunId === taskRunId && 'red' }}
            data-id="name"
          >
            <Highlighter
              highlightClassName={isSelected ? style.highlightSelected : style.highlight}
              searchWords={[searchWords]}
              textToHighlight={ganttRenderData.name}
            />
          </Link>
          <Link to={`/operation-center/task-run-id/${ganttRenderData.taskRunId}`} style={{ zIndex: 999 }} target="_blank">
            &nbsp;&nbsp;&nbsp;{t('operationCenter.runningStatistics.task.jumpToInstance')}
          </Link>
        </div>
      </Popover>
    </div>
  );

});