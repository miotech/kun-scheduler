import React, { memo, useRef } from 'react';
import {
  DailyTaskFinishCount, DailyTaskFinishLineChart
} from '@/components/Monitoring/DailyTaskFinishLineChart/DailyTaskFinishLineChart';
import { Card } from 'antd';
import { useSize } from 'ahooks';
import useI18n from '@/hooks/useI18n';
import { KunSpin } from '@/components/KunSpin';

interface OwnProps {
  data: DailyTaskFinishCount[];
  loading?: boolean;
}

type Props = OwnProps;

export const DailyTaskFinishCountChart: React.FC<Props> = memo(function DailyTaskFinishCountChart(props) {
  const t = useI18n();

  const chartWrapperRef: React.RefObject<any> = useRef<any>();

  const {
    width = 468,
    height = 468,
  } = useSize(chartWrapperRef);

  const { data, loading } = props;

  if (loading) {
    return (
      <Card bodyStyle={{ padding: '8px' }}>
        <h3>{t('monitoringDashboard.dataDevelopment.dailyTaskFinishCountChart.title')}</h3>
        <div ref={chartWrapperRef}>
          <KunSpin asBlock />
        </div>
      </Card>
    );
  }

  if (!data || !data.length) {
    return (
      <Card bodyStyle={{ padding: '8px' }}>
        <h3>{t('monitoringDashboard.dataDevelopment.dailyTaskFinishCountChart.title')}</h3>
        <div ref={chartWrapperRef} className="no-data">No Data</div>
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
