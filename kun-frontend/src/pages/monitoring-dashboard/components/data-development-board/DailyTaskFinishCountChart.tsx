import React, { memo, useRef } from 'react';
import { DailyTaskFinishBarChart } from '@/components/Monitoring/DailyTaskFinishLineChart';
import { Card } from 'antd';
import useRedux from '@/hooks/useRedux';
import useI18n from '@/hooks/useI18n';
import { KunSpin } from '@/components/KunSpin';
import { DailyTaskFinishCountTable } from './DailyTaskFinishCountTable';

export const DailyTaskFinishCountChart: React.FC = memo(function DailyTaskFinishCountChart() {
  const t = useI18n();

  const chartWrapperRef: React.RefObject<any> = useRef<any>();
  const {
    selector: {
      dataDevelopmentBoardData,
    },
  } = useRedux(s => ({
    dataDevelopmentBoardData: s.monitoringDashboard.dataDevelopmentBoardData,
  }));

  const {
    dailyStatisticList,
  } = dataDevelopmentBoardData;

  const { data, loading } = dailyStatisticList;


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

  if (!data.length) {
    return (
      <Card bodyStyle={{ padding: '8px' }}>
        <h3>{t('monitoringDashboard.dataDevelopment.dailyTaskFinishCountChart.title')}</h3>
        <div ref={chartWrapperRef} className="no-data">
          No Data
        </div>
      </Card>
    );
  }

  return (
    <Card bodyStyle={{ padding: '8px'}}>
      <h3>{t('monitoringDashboard.dataDevelopment.dailyTaskFinishCountChart.title')}</h3>
      <div ref={chartWrapperRef} style={{ position: 'relative', width: '100%',overflowX:'scroll' }}>
        <div style={{paddingLeft:'100px'}}>
        <DailyTaskFinishBarChart
          data={data}
          width={766}
          height={460}
        />
        </div>
        <DailyTaskFinishCountTable  data={data} />
      </div>
    </Card>
  );
});
