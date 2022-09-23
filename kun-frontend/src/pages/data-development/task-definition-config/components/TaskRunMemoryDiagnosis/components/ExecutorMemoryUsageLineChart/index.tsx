import React, { useMemo } from 'react';
import ReactEchart, { EChartsOption } from 'echarts-for-react';
import { TaskRunDiagnosis } from '@/definitions/TaskRun.type';
import useI18n from '@/hooks/useI18n';

interface Props {
  data: TaskRunDiagnosis[];
}

const ExecutorMemoryUsageLineChart: React.FC<Props> = ({ data }) => {
  const t = useI18n();
  const option: EChartsOption = useMemo(() => {
    const xAxisData: string[] = [];
    const legendData: string[] = [
      'taskRun.diagnosis.lineChart.legend.executor.num.peak',
      'taskRun.diagnosis.lineChart.legend.executor.num.median',
      'taskRun.diagnosis.lineChart.legend.executor.memory.peak',
      'taskRun.diagnosis.lineChart.legend.executor.memory.median',
      'taskRun.diagnosis.lineChart.legend.driver.memory.peak',
      'taskRun.diagnosis.lineChart.legend.driver.memory.median',
    ].map(i18nKey => t(i18nKey));
    const seriesProps: Array<keyof TaskRunDiagnosis> = [
      'maxExecutorNumber',
      'medianExecutorNumber',
      'maxExecutorMemory',
      'medianExecutorMemory',
      'maxDriverMemory',
      'medianDriverMemory',
    ];
    const series: EChartsOption['series'] = legendData.map(name => ({
      name,
      type: 'line',
      data: [],
    }));
    data.forEach(item => {
      xAxisData.push(item.taskRunId);
      seriesProps.forEach((prop, index) => {
        series[index].data.push(item[prop]);
        if (prop.includes('Number')) {
          series[index].yAxisIndex = 1;
        }
      });
    });
    return {
      title: {
        text: t('taskRun.diagnosis.lineChart.totalMemoryUsage.title'),
        left: 'center',
      },
      grid: {
        containLabel: true,
      },
      tooltip: {
        trigger: 'axis',
      },
      legend: {
        data: legendData,
        bottom: 0,
      },
      xAxis: {
        name: t('taskRun.diagnosis.lineChart.xAxis.name'),
        type: 'category',
        data: xAxisData,
      },
      yAxis: [
        {
          name: `${t('taskRun.diagnosis.lineChart.yAxis.name.memoryValue')}(MB)`,
          type: 'value',
        },
        {
          name: t('taskRun.diagnosis.lineChart.yAxis.name.executorNum'),
          type: 'value',
          alignTicks: true,
        },
      ],
      series,
    };
  }, [data, t]);
  return <ReactEchart option={option} />;
};

export default ExecutorMemoryUsageLineChart;
