// @ts-noCheck

import React, { memo, useMemo, useCallback, useState, useRef } from 'react';
import moment from 'moment';
import { Tasks } from '@/definitions/Gantt.type';
import useI18n from '@/hooks/useI18n';
import style from './Gantt.less';
import { BarRender } from './BarRender';

interface OwnProps {
  data: Tasks;
  taskRunId: String | null;
  searchWords: string;
  currentIndex: number;
}

// 求时间中间隔多少h
function getIntervalHour(startDate: string, endDate: string) {
  const ms = moment(endDate).valueOf() - moment(startDate).valueOf();
  if (ms < 0) return 0;
  return Math.floor(ms / 1000 / 60 / 60);
}



export const Gantt: React.FC<OwnProps> = memo(function Gantt(props) {
  const t = useI18n();
  const { data, taskRunId, searchWords, currentIndex } = props;
  const toolbarRef = useRef();
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



  const closeCurrentTime = useCallback(() => {

    document.getElementById('line').style.display = 'none';
    document.getElementById('currentTime').style.display = 'none';
  }, []);

  const onBarScroll = useCallback(
    e => {

      const { clientHeight, scrollHeight, scrollTop, scrollLeft } = e.currentTarget;
      const scrollOffset = Math.max(
        0,
        Math.min(scrollTop, scrollHeight - clientHeight)
      );
      setOffsetState(scrollOffset);
    
      document.getElementById('toolbar').style.paddingRight = `${scrollLeft + 200}px`; // +200是为了时间范围小时撑开时间轴
      document.getElementById('toolbar').scrollLeft = scrollLeft;
      closeCurrentTime();
    },
    [closeCurrentTime],
  );


  const [startIndex, stopIndex] = useMemo(() => {
    const start = Math.ceil(offsetState / 40) - 5;
    const stop = start + 30;
    return [start, stop];
  }, [offsetState]);


  return (
    <div className={style.content} onMouseLeave={() => closeCurrentTime()}>
      <div className={style.line} id="line" style={{ display: 'none' }} />
      <div className={style.currentTime} id="currentTime" style={{ display: 'none' }}>
        {' '}
      </div>
      <div className={style.bar} id="bar" onScroll={onBarScroll}>
        <div style={{ height: `${infoList.length * 40}px`, width: '100%' }} id="container">
          {infoList.map((item, index) => {
            if (index >= startIndex && index <= stopIndex) {
              return (
                <BarRender
                  searchWords={searchWords}
                  isSelected={searchWords && currentIndex === index}
                  top={index * 40}
                  task={item}
                  key={item.id}
                  toolBarStartTime={toolBarStartTime}
                  taskRunId={taskRunId}
                  infoList={infoList}
                />
              );
            }
          })}
        </div>
      </div>
      <div className={style.toolBar} id="toolbar" ref={toolbarRef}>
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
    </div>
  );
});
