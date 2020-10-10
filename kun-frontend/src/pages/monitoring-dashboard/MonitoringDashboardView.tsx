import React, { memo } from 'react';
import useI18n from '@/hooks/useI18n';
import { Col, Row } from 'antd';
import { useMount, useUnmount } from 'ahooks';
import useRedux from '@/hooks/useRedux';

import DataDiscoveryBoard from './components/data-discovery-board';
import DataDevelopmentBoard from './components/data-development-board';

import styles from './MonitoringDashboardView.less';

interface OwnProps {}

type Props = OwnProps;

export const MonitoringDashboardView: React.FC<Props> = memo(function MonitoringDashboardView() {
  const t = useI18n();
  const { selector: {
    viewState,
  }, dispatch } = useRedux(state => ({
    viewState: state.monitoringDashboard,
  }));

  useMount(() => {
    if (!viewState.allSettled) {
      dispatch.monitoringDashboard.reloadAll(viewState);
    }
  });

  useUnmount(() => {
    dispatch.monitoringDashboard.resetAll();
  });

  return (
    <div id="monitoring-dashboard-view" className={styles.View}>
      <Row>
        {/* Left panel: data discovery monitoring boards */}
        <Col xl={12} md={24} className={styles.Col}>
          <div className={styles.PrimaryDivision}>
            <h2>{t('monitoringDashboard.dataDiscovery.title')}</h2>
            <DataDiscoveryBoard />
          </div>
        </Col>
        {/* Right panel: data development monitoring boards */}
        <Col xl={12} md={24} className={styles.Col}>
          <div className={styles.PrimaryDivision}>
            <h2>{t('monitoringDashboard.dataDevelopment.title')}</h2>
            <DataDevelopmentBoard />
          </div>
        </Col>
      </Row>
    </div>
  );
});
