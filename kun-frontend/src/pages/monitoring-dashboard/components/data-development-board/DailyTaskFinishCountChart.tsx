import React, { memo, useMemo, useRef } from 'react';
import { DailyTaskFinishCount } from '@/components/Monitoring/DailyTaskFinishLineChart/DailyTaskFinishLineChart';
import { DailyTaskFinishBarChart } from '@/components/Monitoring/DailyTaskFinishLineChart';
import { Card } from 'antd';
import useRedux from '@/hooks/useRedux';
import useI18n from '@/hooks/useI18n';
import { KunSpin } from '@/components/KunSpin';
import { dayjs } from '@/utils/datetime-utils';
import { DailyTaskFinishCountTable } from './DailyTaskFinishCountTable';

export const DailyTaskFinishCountChart: React.FC = memo(function DailyTaskFinishCountChart() {
  const t = useI18n();

  const chartWrapperRef: React.RefObject<any> = useRef<any>();

  // const { width = 468, height = 468 } = useSize(chartWrapperRef);
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
  const normalizedData = useMemo(() => {
    if (data && data.length) {
      return data.map((datum: DailyTaskFinishCount) => ({
        ...datum,
        time: dayjs(datum.time)
          .startOf('day')
          .toDate()
          .getTime(),
      }));
    }
    // else
    return [];
  }, [data]);

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

  if (!normalizedData.length) {
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
        <div style={{paddingLeft:'110px'}}>
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
