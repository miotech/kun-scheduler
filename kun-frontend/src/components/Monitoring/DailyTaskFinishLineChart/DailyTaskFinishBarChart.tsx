import React, { memo } from 'react';
import ReactEcharts from 'echarts-for-react';

interface OwnProps {
  width: number;
  height: number;
  // data?: [];
}

type Props = OwnProps;

export const DailyTaskFinishBarChart: React.FC<Props> = memo(function DailyTaskFinishBarChart(props) {
  const {
    width = 1024,
    height = 768,
    // data = [],
  } = props;
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
        data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun', '12'],
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
        data: [320, 332, 301, 334, 390, 330, 320],
      },
      {
        name: 'FAILED',
        type: 'bar',
        stack: 'Ad',
        emphasis: {
          focus: 'series',
        },
        data: [120, 132, 101, 134, 90, 230, 210],
      },
      {
        name: 'UPSTREAM_FAILED',
        type: 'bar',
        stack: 'Ad',
        emphasis: {
          focus: 'series',
        },
        data: [220, 182, 191, 234, 290, 330, 310],
      },
      {
        name: 'ABORTED',
        type: 'bar',
        stack: 'Ad',
        emphasis: {
          focus: 'series',
        },
        data: [150, 232, 201, 154, 190, 330, 410],
      },
      {
        name: 'ONGOING',
        type: 'bar',
        stack: 'Ad',
        data: [862, 1018, 964, 1026, 1679, 1600, 1570],
        emphasis: {
          focus: 'series',
        },
      },
    ],
  };
  return <ReactEcharts style={{ height, width }} option={defaultOption} />;
});
