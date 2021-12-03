import React, { memo } from 'react';
import ReactEcharts from 'echarts-for-react';
import { DailyStatistic } from '@/services/monitoring-dashboard';
import { dayjs } from '@/utils/datetime-utils';

interface OwnProps {
  width: number;
  height: number;
  data: DailyStatistic[];
}

type Props = OwnProps;

export const DailyTaskFinishBarChart: React.FC<Props> = memo(function DailyTaskFinishBarChart(props) {
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
    xAxis.push(dayjs(item.time).format('MM-DD'))
    sucList.push(item.taskResultList.find(idx=>idx.status === 'SUCCESS')?.taskCount || 0)
    faiList.push(item.taskResultList.find(idx=>idx.status === 'FAILED')?.taskCount || 0)
    upsList.push(item.taskResultList.find(idx=>idx.status === 'UPSTREAM_FAILED')?.taskCount || 0)
    aboList.push(item.taskResultList.find(idx=>idx.status === 'ABORTED')?.taskCount || 0)
    ongList.push(item.taskResultList.filter(idx=>idx.status === 'ONGOING').reduce((accumulator, currentValue)=>(accumulator + currentValue.taskCount),0))
  })
  const defaultOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow',
      },
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
      },
    ],
    series: [
      {
        name: 'SUCCESS',
        type: 'bar',
        stack: 'Ad',
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
  return <ReactEcharts style={{ height, width }} option={defaultOption} />;
});
