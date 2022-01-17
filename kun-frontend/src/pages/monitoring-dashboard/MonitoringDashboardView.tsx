import React, { memo, useMemo } from 'react';
import useI18n from '@/hooks/useI18n';
import { Button, Col, Row } from 'antd';
import { ReloadOutlined } from '@ant-design/icons';
import { useMount } from 'ahooks';
import useRedux from '@/hooks/useRedux';

import DataDiscoveryBoard from './components/data-discovery-board';
import DataDevelopmentBoard from './components/data-development-board';

import styles from './MonitoringDashboardView.less';

interface OwnProps {}

type Props = OwnProps;

export const MonitoringDashboardView: React.FC<Props> = memo(function MonitoringDashboardView() {
  const t = useI18n();
  const {
    selector: { viewState, allSettled },
    dispatch,
  } = useRedux(state => ({
    viewState: state.monitoringDashboard,
    allSettled: state.monitoringDashboard.allSettled,
  }));
  let showloading = true;
  if (allSettled) {
    showloading = false;
  }
  useMount(() => {
    dispatch.monitoringDashboard.reloadAll({
      viewState,
      showloading,
    });
  });

  const floatRefreshButton = useMemo(() => {
    return (
      <div className={styles.ReloadBtnWrapper}>
        <Button
          icon={<ReloadOutlined />}
          onClick={() => {
            dispatch.monitoringDashboard.reloadAll({
              viewState,
              showloading: true,
            });
          }}
          loading={!allSettled}
        >
          {t('common.refresh')}
        </Button>
      </div>
    );
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [allSettled, t]);

  return (
    <div id="monitoring-dashboard-view" className={styles.View}>
      {floatRefreshButton}
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
