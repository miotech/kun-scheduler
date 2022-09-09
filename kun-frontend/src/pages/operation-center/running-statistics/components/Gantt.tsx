// @ts-noCheck

import React, { memo, useMemo, useCallback, useState, useRef } from 'react';
import moment from 'moment';
import { Tasks } from '@/definitions/Gantt.type';
import useI18n from '@/hooks/useI18n';
import { Link } from 'umi';
import { Popover } from 'antd';
import useUrlState from '@ahooksjs/use-url-state';
import style from './Gantt.less';
import { TooltipHtml } from './TooltipHtml';
import { WaitTask } from './WaitTask';

interface OwnProps {
  data: Tasks;
  taskRunId: String | null;
}

// 求时间中间隔多少h
function getIntervalHour(startDate: string, endDate: string) {
  const ms = moment(endDate).valueOf() - moment(startDate).valueOf();
  if (ms < 0) return 0;
  return Math.floor(ms / 1000 / 60 / 60);
}

// 求时间中间隔多少m
function getIntervalMinutes(startDate: string, endDate: string) {
  const ms = moment(endDate).valueOf() - moment(startDate).valueOf();
  if (ms < 0) return 0;
  return Math.floor(ms / 1000 / 60);
}

export const Gantt: React.FC<OwnProps> = memo(function Gantt(props) {
  const t = useI18n();
  const { data, taskRunId } = props;
  const toolbarRef = useRef(null);
  const [routeState, setRouteState] = useUrlState({});
  const [offsetState, setOffsetState] = useState(0);
  const { earliestTime, latestTime, infoList } = data;
  const toolBarStartTime = useMemo(() => moment(earliestTime).format('YYYY-MM-DD, HH:00:00'), [earliestTime]); // 坐标轴开始时间
  // 时间轴有多少格
  const count = useMemo(() => {
    if (earliestTime && latestTime) {
      const res = getIntervalHour(earliestTime, latestTime);
      const timeItemCount = parseInt(res / 2, 10) + 1;
      return timeItemCount < 12 ? 12 : timeItemCount;
    }
    return 0;
  }, [earliestTime, latestTime]);
  const countArray = useCallback(() => {
    const arr = [];
    for (let i = 0; i < count; i += 1) {
      arr.push(i);
    }
    return arr || [];
  }, [count]);

  const onMouseMove = useCallback(
    e => {
      const left = e.clientX - 86;
      const dataId = e.target.getAttribute('data-id');
      if (['barRunFailed', 'barRun', 'barWait'].includes(dataId)) {
        document.getElementById('line').style.transform = `translateX(${left}px)`;
        document.getElementById('line').style.display = 'block';

        document.getElementById('currentTime').style.transform = `translateX(${left - 50}px)`;
        document.getElementById('currentTime').style.display = 'block';

        const timeSize = left + document.getElementById('toolbar').scrollLeft;
        document.getElementById('currentTime').innerHTML = moment(toolBarStartTime)
          .add(timeSize, 'm')
          .format('MM-DD, HH:mm');
      }
    },
    [toolBarStartTime],
  );

  const res = useMemo(
    () =>
      infoList.map(item => {
        const waitLeft = item.queuedAt ? getIntervalMinutes(toolBarStartTime, item.queuedAt) || 1 : 0;
        const waitWidth = item.queuedAt && item.startAt ? getIntervalMinutes(item.queuedAt, item.startAt) || 1 : 0;
        const runWidth = item.startAt ? getIntervalMinutes(item.startAt, item.endAt) || 1 : 0;
        return {
          waitLeft,
          waitWidth,
          runWidth,
          status: item.status,
          taskRunId: item.taskRunId,
          taskId: item.taskId,
          averageWidth: item.averageRunningTime ? parseInt(item.averageRunningTime / 60, 10) : 0,
          name: item.name,
        };
      }),
    [toolBarStartTime, infoList],
  );

  const closeCurrentTime = useCallback(() => {

    document.getElementById('line').style.display = 'none';
    document.getElementById('currentTime').style.display = 'none';
  }, []);

  const onBarScroll = useCallback(
    e => {

      const { clientHeight, scrollHeight, scrollTop, scrollLeft } = e.currentTarget;
      console.log(scrollLeft);

      const scrollOffset = Math.max(
        0,
        Math.min(scrollTop, scrollHeight - clientHeight)
      );
      setOffsetState(scrollOffset);
      const {clientWidth} = toolbarRef.current;
      document.getElementById('toolbar').style.paddingRight = `${scrollLeft}px`;
      document.getElementById('toolbar').scrollLeft = scrollLeft;
      closeCurrentTime();
    },
    [closeCurrentTime, toolbarRef],
  );


  const changePadding = (dom: any) => {
    if (dom) {
      // const { scrollWidth } = document.getElementById('container');
      // const toolbarScrollWidth = dom.scrollWidth;


    }
  };

  const openDrawer = useCallback(
    (id: string) => {
      setRouteState({
        waitForTaskRunId: id,
      });
    },
    [setRouteState],
  );

  // const _getRangeToRender = ()  => { 
  const [startIndex, stopIndex] = useMemo(() => {
    const start = Math.floor(offsetState / 40) - 5;
    const stop = start + 25;
    return [start, stop];
  }, [offsetState]);

  // };
  // const [startIndex, stopIndex] = _getRangeToRender();



  return (
    <div className={style.content} onMouseLeave={() => closeCurrentTime()}>
      <div className={style.line} id="line" style={{ display: 'none' }} />
      <div className={style.currentTime} id="currentTime" style={{ display: 'none' }}>
        {' '}
      </div>
      <div className={style.bar} id="bar" onScroll={onBarScroll}>

        <div style={{ height: `${res.length * 40}px`, width: '100%' }} id="container">

          {res.map((item, index) => {
            if (index >= startIndex && index <= stopIndex) {
              return (
                <div className={style.barItem} style={{ top: `${index * 40}px` }} key={item.taskRunId}>
                  <Popover
                    content={
                      <TooltipHtml
                        waitTime={item.waitWidth}
                        runTime={item.runWidth}
                        averageRunTime={item.averageWidth}
                        taskRunId={item.taskRunId}
                        openDrawer={openDrawer}
                      />
                    }
                    mouseEnterDelay={0.3}
                    placement="topLeft"
                    destroyTooltipOnHide
                    trigger="hover"
                  >
                    <div
                      key={item.taskRunId}
                      onMouseMove={e => onMouseMove(e)}
                      className={style.barCon}
                      style={{ transform: `translateX(${item.waitLeft}px)` }}
                    >
                      <div
                        className={style.barWait}
                        onClick={() => openDrawer(item.taskRunId)}
                        style={{ width: `${item.waitWidth}px` }}
                        data-id="barWait"
                      />
                      {item.status === 'SUCCESS' && (
                        <div className={style.barRun} style={{ width: `${item.runWidth}px` }} data-id="barRun" />
                      )}
                      {item.status === 'FAILED' && (
                        <div
                          className={style.barRunFailed}
                          style={{
                            width: `${item.runWidth}px`,
                          }}
                          data-id="barRunFailed"
                        />
                      )}

                      <div
                        className={style.average}
                        style={{ width: `${item.averageWidth}px`, transform: `translateX(${item.waitWidth}px)` }}
                      />
                      <Link
                        to={`/operation-center/running-statistics?taskRunId=${item.taskRunId}&taskName=${item.name}`}
                        className={style.name}
                        style={{ color: item.taskRunId === taskRunId && 'red' }}
                        data-id="name"
                      >
                        {item.name}
                      </Link>
                      <Link to={`/operation-center/task-run-id/${item.taskRunId}`} style={{ zIndex: 999 }} target="_blank">
                        &nbsp;&nbsp;&nbsp;{t('operationCenter.runningStatistics.task.jumpToInstance')}
                      </Link>
                    </div>
                  </Popover>
                </div>
              );
            }
          })}
        </div>
      </div>

      <div className={style.toolBar} id="toolbar" ref={toolbarRef}>
        <div className={style.bottom} id="bottom">
          {countArray().map((item, index) => (
            <div key={item} className={style.timeItem}>
              {moment(earliestTime)
                .add(index * 2 + 1, 'hours')
                .format('MM-DD, HH:00')}
            </div>
          ))}
          <div className={style.title}>{t('operationCenter.runningStatistics.timeLine')}</div>
        </div>
      </div>
      <WaitTask drawerVisible={routeState?.waitForTaskRunId} waitForTaskRunId={routeState?.waitForTaskRunId} />
    </div>
  );
});
