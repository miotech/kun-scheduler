import React from 'react';
import { Meta } from '@storybook/react';
import {
  DailyTaskFinishCount, DailyTaskFinishLineChart
} from '@/components/Monitoring/DailyTaskFinishLineChart/DailyTaskFinishLineChart';

export default {
  title: 'components/Monitoring/DailyTaskFinishLineChart',
  component: DailyTaskFinishLineChart,
} as Meta;

const data: DailyTaskFinishCount[] = [
  {
    taskCount: 56,
    // 1595257200000
    time: 1595228400000,
  },
  {
    taskCount: 32,
    time: 1595314800000,
  },
  {
    taskCount: 79,
    time: 1595401200000,
  },
  {
    taskCount: 41,
    time: 1595487600000,
  },
  {
    taskCount: 93,
    time: 1595574000000,
  }
];

export const DailyTaskFinishLineChartDemo = () => {
  return (
    <DailyTaskFinishLineChart
      width={900}
      height={600}
      data={data}
    />
  );
};
