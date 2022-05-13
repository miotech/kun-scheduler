// @ts-noCheck

import React, { memo, useMemo, useCallback } from 'react';
import moment from 'moment';
import ReactTooltip from 'react-tooltip';
import ReactDOMServer from 'react-dom/server';
import { Tasks } from '@/definitions/Gantt.type';
import useI18n from '@/hooks/useI18n';
import { Link } from 'umi';
import style from './Gantt.less';
import { TooltipHtml } from './TooltipHtml';

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

  const setTooltipValue = useCallback(item => {
    document.getElementById('gantt-tooltip-waitTime').innerHTML = `${item.waitWidth} min`;
    document.getElementById('gantt-tooltip-runTime').innerHTML = `${item.runWidth} min`;
    document.getElementById('gantt-tooltip-averageRunTime').innerHTML = `${item.averageWidth} min`;
  }, []);

  const onMouseMove = useCallback(
    (e, item) => {
      const left = e.clientX - 86;
      if (e.target.getAttribute('data-id') !== 'name') {
        document.getElementById('line').style.transform = `translateX(${left}px)`;
        document.getElementById('line').style.display = 'block';

        document.getElementById('currentTime').style.transform = `translateX(${left - 50}px)`;
        document.getElementById('currentTime').style.display = 'block';

        const timeSize = left + document.getElementById('toolbar').scrollLeft;
        document.getElementById('currentTime').innerHTML = moment(toolBarStartTime)
          .add(timeSize, 'm')
          .format('MM-DD, HH:mm');
      }
      setTooltipValue(item);
    },
    [toolBarStartTime, setTooltipValue],
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
      document.getElementById('toolbar').scrollLeft = e.target.scrollLeft;
      closeCurrentTime();
    },
    [closeCurrentTime],
  );

  const changePadding = (dom: any) => {
    if (dom) {
      const barScrollWidth = document.getElementById('bar').scrollWidth;
      const toolbarScrollWidth = dom.scrollWidth;
      document.getElementById('toolbar').style.paddingRight = `${barScrollWidth - toolbarScrollWidth}px`;
    }
  };
  return (
    <div className={style.content} onMouseLeave={() => closeCurrentTime()}>
      <div className={style.line} id="line" style={{ display: 'none' }} />
      <div className={style.currentTime} id="currentTime" style={{ display: 'none' }}>
        {' '}
      </div>
      <div className={style.bar} id="bar" onScroll={onBarScroll}>
        {res.map(item => {
          return (
            <div className={style.barItem} key={item.taskRunId} data-id="barItem">
              <div
                key={item.taskRunId}
                onMouseMove={e => onMouseMove(e, item)}
                className={style.barCon}
                style={{ transform: `translateX(${item.waitLeft}px)` }}
                data-tip={ReactDOMServer.renderToString(<TooltipHtml t={t} />)}
                data-html
              >
                <div className={style.barWait} style={{ width: `${item.waitWidth}px` }} />
                {item.status === 'SUCCESS' && <div className={style.barRun} style={{ width: `${item.runWidth}px` }} />}
                {item.status === 'FAILED' && (
                  <div className={style.barRunFailed} style={{ width: `${item.runWidth}px` }} />
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
            </div>
          );
        })}
      </div>
      <div className={style.toolBar} id="toolbar" ref={dom => changePadding(dom)}>
        <div className={style.bottom}>
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
      <ReactTooltip offset={{ bottom: 10, right: 10 }} backgroundColor="#fff" />
    </div>
  );
});
