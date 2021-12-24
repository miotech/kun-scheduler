import React, { memo } from 'react';
import ReactEcharts from 'echarts-for-react';
import { DailyStatistic } from '@/services/monitoring-dashboard';
import { dayjs } from '@/utils/datetime-utils';
import useRedux from '@/hooks/useRedux';

interface OwnProps {
  width: number;
  height: number;
  data: DailyStatistic[];
}

type Props = OwnProps;

const timeToWeek = (time: string) => {
  const day = dayjs(time as string).day();
  switch (day) {
    case 0:
      return 'Sun';
    case 1:
      return 'Mon';
    case 2:
      return 'Tue';
    case 3:
      return 'Wed';
    case 4:
      return 'Thu';
    case 5:
      return 'Fri';
    case 6:
      return 'Sat';
    default:
      return '';
  }
};

export const DailyTaskFinishBarChart: React.FC<Props> = memo(function DailyTaskFinishBarChart(props) {
  const timeCache: string[] = []
  const { dispatch } = useRedux(() => ({}));
  const onChartClick = (obj:any) => {
    const targetTime = timeCache[obj.dataIndex]
    const params = {
      targetTime,
      status: obj.seriesName,
      finalStatus: obj.seriesName === 'ONGOING' ? '' : obj.seriesName,
      showId: null,
      timezoneOffset: 8,
    }
    dispatch.monitoringDashboard.setTaskDetailsForWeekParams(params);
  }
  const onEvents = {
    'click': onChartClick,
  }
  const {
    width = 1024,
    height = 768,
    data = [],
  } = props;
  const xAxis:string[] = []
  const sucList:number[] = []
  const faiList:number[] = []
  const upsList:number[] = []
  const aboList:number[] = []
  const ongList:number[] = []
  data.forEach(item=> {
    xAxis.push(`${timeToWeek(item.time)},${dayjs(item.time).format('MM-DD')}`)
    timeCache.push(item.time)
    sucList.push(item.taskResultList.find(idx=>idx.status === 'SUCCESS')?.taskCount || 0)
    faiList.push(item.taskResultList.find(idx=>idx.status === 'FAILED')?.taskCount || 0)
    upsList.push(item.taskResultList.find(idx=>idx.status === 'UPSTREAM_FAILED')?.taskCount || 0)
    aboList.push(item.taskResultList.find(idx=>idx.status === 'ABORTED')?.taskCount || 0)
    ongList.push(item.taskResultList.filter(idx=>idx.status === 'ONGOING').reduce((accumulator, currentValue)=>(accumulator + currentValue.taskCount),0))
  })
  const defaultOption = {
    tooltip: {
      trigger: 'axis',
      triggerOn: 'click',
      axisPointer: {
        type: 'shadow',
      },
    },
    textStyle: {
      color: '#9c9c9c'
    },
    color: ['#9bc655', 'rgb(238,108,69)', '#8f5a2c', '#ffb5cd', '#3ec3cb'],
    legend: {},
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true,
    },
    xAxis: [
      {
        type: 'category',
        data: xAxis,
      },
    ],
    yAxis: [
      {
        type: 'value',
        axisLine: {
          show: true,
        },
        name: 'Count'
      },
    ],
    series: [
      {
        name: 'SUCCESS',
        type: 'bar',
        stack: 'Ad',
        barWidth: '45%',
        emphasis: {
          focus: 'series',
        },
        data: sucList,
      },
      {
        name: 'FAILED',
        type: 'bar',
        stack: 'Ad',
        emphasis: {
          focus: 'series',
        },
        data: faiList,
      },
      {
        name: 'UPSTREAM_FAILED',
        type: 'bar',
        stack: 'Ad',
        emphasis: {
          focus: 'series',
        },
        data: upsList,
      },
      {
        name: 'ABORTED',
        type: 'bar',
        stack: 'Ad',
        emphasis: {
          focus: 'series',
        },
        data: aboList,
      },
      {
        name: 'ONGOING',
        type: 'bar',
        stack: 'Ad',
        data: ongList,
        emphasis: {
          focus: 'series',
        },
      },
    ],
  };
  return <ReactEcharts  onEvents={onEvents} style={{ height, width }} option={defaultOption} />;
});
