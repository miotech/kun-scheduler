import React, { memo, useRef } from 'react';
import {
  DailyTaskFinishCount, DailyTaskFinishLineChart
} from '@/components/Monitoring/DailyTaskFinishLineChart/DailyTaskFinishLineChart';
import { Card } from 'antd';
import { useSize } from 'ahooks';
import useI18n from '@/hooks/useI18n';

interface OwnProps {
  data: DailyTaskFinishCount[];
}

type Props = OwnProps;

export const DailyTaskFinishCountChart: React.FC<Props> = memo((props) => {
  const t = useI18n();

  const chartWrapperRef: React.RefObject<any> = useRef<any>();

  const {
    width = 468,
    height = 468,
  } = useSize(chartWrapperRef);

  const { data } = props;

  if (!data || !data.length) {
    return (
      <Card bodyStyle={{ padding: '8px' }}>
        <h3>{t('monitoringDashboard.dataDevelopment.dailyTaskFinishCountChart.title')}</h3>
        <div className="no-data">No Data</div>
      </Card>
    );
  }

  return (
    <Card bodyStyle={{ padding: '8px' }}>
      <h3>{t('monitoringDashboard.dataDevelopment.dailyTaskFinishCountChart.title')}</h3>
      <div ref={chartWrapperRef} style={{ position: 'relative', width: '100%', height: '468px' }}>
        <DailyTaskFinishLineChart
          data={data || []}
          // Avoid cases that consistently invoke `useSize` updates
          width={width - 8}
          height={height - 8}
        />
      </div>
    </Card>
  );
});
