import React, { useMemo } from 'react';
import ReactEchart from 'echarts-for-react';
import { EChartsOption } from 'echarts';
import { TaskRunDiagnosis } from '@/definitions/TaskRun.type';
import useI18n from '@/hooks/useI18n';

interface Props {
  data: TaskRunDiagnosis[];
}

const MemoryUsageLineChart: React.FC<Props> = ({ data }) => {
  const t = useI18n();
  const option: EChartsOption = useMemo(() => {
    const memoryUsageTxt = t('taskRun.diagnosis.lineChart.legend.memoryUsage');
    const memoryUsagePercentTxt = t('taskRun.diagnosis.lineChart.legend.memoryUsagePercent');
    const xAxisData: string[] = [];
    const memoryUsageData: string[] = [];
    const memoryUsagePercentData: string[] = [];
    data.forEach(({ memorySeconds, memoryUsagePercentage, taskRunId }) => {
      xAxisData.push(taskRunId);
      memoryUsageData.push(memorySeconds);
      memoryUsagePercentData.push(memoryUsagePercentage);
    });
    return {
      title: {
        text: t('taskRun.diagnosis.lineChart.totalMemoryUsage.title'),
        left: 'center',
      },
      legend: {
        data: [memoryUsageTxt, memoryUsagePercentTxt],
        bottom: 0,
      },
      grid: {
        bottom: '10%',
        containLabel: true,
      },
      tooltip: {
        trigger: 'axis',
      },
      xAxis: {
        name: t('taskRun.diagnosis.lineChart.xAxis.name'),
        type: 'category',
        data: xAxisData,
      },
      yAxis: [
        {
          name: `${memoryUsageTxt}(MB-seconds)`,
          type: 'value',
        },
        {
          name: `${memoryUsagePercentTxt}(%)`,
          type: 'value',
          alignTicks: true,
        },
      ],
      series: [
        {
          name: memoryUsageTxt,
          type: 'line',
          data: memoryUsageData,
        },
        {
          name: memoryUsagePercentTxt,
          type: 'line',
          yAxisIndex: 1,
          data: memoryUsagePercentData,
        },
      ],
    };
  }, [data, t]);
  return <ReactEchart option={option} />;
};

export default MemoryUsageLineChart;
