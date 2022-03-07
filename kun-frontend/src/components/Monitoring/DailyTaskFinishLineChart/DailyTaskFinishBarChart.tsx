import React, { memo } from 'react';
import ReactEcharts, { EChartsInstance } from 'echarts-for-react';
import { DailyStatistic } from '@/services/monitoring-dashboard';
import { dayjs } from '@/utils/datetime-utils';
import useRedux from '@/hooks/useRedux';
import { taskColorConfig } from '@/constants/colorConfig';

interface OwnProps {
  width: number;
  height: number;
  data: DailyStatistic[];
}
interface BarSelectCache {
  dataIndex: number,
  seriesIndex: number
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

const commonConfig = {
  type: 'bar',
  stack: 'Ad',
  emphasis: {
    focus: 'series',
  },
  selectedMode: 'single',
  select: {
    itemStyle: {
      borderColor: '#7A7E87',
      shadowColor: 'rgba(0, 0, 0, 0.5)',
      borderWidth: 2,
      shadowBlur: 10
    }
  },
};

export const DailyTaskFinishBarChart: React.FC<Props> = memo(function DailyTaskFinishBarChart(props) {
  const timeCache: string[] = [];
  const {
    selector: {
      clearBarChart
    },
    dispatch,
  } = useRedux(s => ({
    clearBarChart: s.monitoringDashboard.dataDevelopmentBoardData.clearBarChart,
  })); 
  let echartsInstance: EChartsInstance = null;
  let barSelectCache: BarSelectCache | null = null;
  const onChartReady = (instance: EChartsInstance) => {
    echartsInstance = instance;
  };



  const deleteBarSelect = () => {
    if (barSelectCache) {
      echartsInstance.dispatchAction({
        type: 'unselect',
        dataIndex: barSelectCache.dataIndex,
        seriesIndex: barSelectCache.seriesIndex
      });
      barSelectCache = null;
    }
  };

  const addBarSelect = (dataIndex: number, seriesIndex: number) => {
    deleteBarSelect();
    echartsInstance.dispatchAction({
      type: 'select',
      dataIndex,
      seriesIndex
    });
    barSelectCache = {
      dataIndex,
      seriesIndex
    };
  };


  const onChartClick = (obj: any) => {
    const targetTime = timeCache[obj.dataIndex];
    const params = {
      targetTime,
      status: obj.seriesName,
      finalStatus: obj.seriesName === 'ONGOING' ? '' : obj.seriesName,
      showId: null,
      timezoneOffset: 8,
      type: 'bar'
    };
    addBarSelect(obj.dataIndex, obj.seriesIndex);
    dispatch.monitoringDashboard.setTaskDetailsForWeekParams(params);
  };
  const onEvents = {
    'click': onChartClick,
  };

  const {
    width = 1024,
    height = 768,
    data = [],
  } = props;
  const xAxis: string[] = [];
  const sucList: number[] = [];
  const faiList: number[] = [];
  const checkFailList: number[] = [];
  const upsList: number[] = [];
  const aboList: number[] = [];
  const ongList: number[] = [];
  data.forEach(item => {
    xAxis.push(`${timeToWeek(item.time)},${dayjs(item.time).format('MM-DD')}`);
    timeCache.push(item.time);
    sucList.push(item.taskResultList.find(idx => idx.status === 'SUCCESS')?.taskCount || 0);
    faiList.push(item.taskResultList.find(idx => idx.status === 'FAILED')?.taskCount || 0);
    checkFailList.push(item.taskResultList.find(idx => idx.status === 'CHECK_FAILED')?.taskCount || 0);
    upsList.push(item.taskResultList.find(idx => idx.status === 'UPSTREAM_FAILED')?.taskCount || 0);
    aboList.push(item.taskResultList.find(idx => idx.status === 'ABORTED')?.taskCount || 0);
    ongList.push(item.taskResultList.filter(idx => idx.status === 'ONGOING').reduce((accumulator, currentValue) => (accumulator + currentValue.taskCount), 0));
  });
  const defaultOption = {
    tooltip: {
      trigger: 'item',
      axisPointer: {
        type: 'shadow',
      },
    },
    textStyle: {
      color: '#9c9c9c'
    },
    color: [taskColorConfig.SUCCESS, taskColorConfig.FAILED,taskColorConfig.CHECK_FAILED, taskColorConfig.UPSTREAM_FAILED, taskColorConfig.ABORTED, '#3ec3cb'],
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
        barWidth: '45%',
        data: sucList,
        ...commonConfig
      },
      {
        name: 'FAILED',
        data: faiList,
        ...commonConfig
      },
      {
        name: 'CHECK_FAILED',
        data: checkFailList,
        ...commonConfig
      },
      {
        name: 'UPSTREAM_FAILED',
        data: upsList,
        ...commonConfig
      },
      {
        name: 'ABORTED',
        data: aboList,
        ...commonConfig
      },
      {
        name: 'ONGOING',
        data: ongList,
        ...commonConfig
      },
    ],
  };
  return <ReactEcharts onChartReady={onChartReady} onEvents={onEvents} style={{ height, width }} option={defaultOption} />;
});
