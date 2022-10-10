import React, { memo, useMemo } from 'react';
import useI18n from '@/hooks/useI18n';
import { Button, Tabs } from 'antd';
import { ReloadOutlined } from '@ant-design/icons';
import { useMount } from 'ahooks';
import useRedux from '@/hooks/useRedux';

import DataDiscoveryBoard from './components/data-discovery-board';
import DataDevelopmentBoard from './components/data-development-board';

import styles from './MonitoringDashboardView.less';

interface OwnProps {}

type Props = OwnProps;

const { TabPane } = Tabs;

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

  const refreshButton = useMemo(() => {
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
      <Tabs tabBarExtraContent={{ right: refreshButton }}>
        <TabPane tab={t('monitoringDashboard.dataKanban.title')} key="dataKanban">
          <DataDiscoveryBoard />
        </TabPane>
        <TabPane tab={t('monitoringDashboard.dataDevelopment.title')} key="dataDevelopment">
          <DataDevelopmentBoard />
        </TabPane>
      </Tabs>
    </div>
  );
});
