import React, { memo } from 'react';
import { Layout, Spin } from 'antd';
import { IRoute } from 'umi';
import { useSelector } from 'react-redux';
import { RootState } from '@/rematch/store';

import useI18n from '@/hooks/useI18n';

import Header from '@/components/Header/Header';
import Sider from '@/components/Sider/Sider';

import css from './NoBreadcrumbLayout.less';

const { Content } = Layout;

interface Props {
  children: React.ReactNode;
  route: IRoute;
}

export default memo(function NoBreadcrumbLayout({ children, route }: Props) {
  const isLoading = useSelector((state: RootState) => state.user.whoamiLoading);

  const t = useI18n();

  return (
    <Spin
      wrapperClassName={css.spinContainer}
      spinning={isLoading}
      tip={t('common.loading')}
    >
      <Layout style={{ height: '100%' }}>
        <Header />
        <Layout className={css.siderAndContent}>
          <Sider route={route} />
          <Content className={css.content}>
            <div>{children}</div>
          </Content>
        </Layout>
      </Layout>
    </Spin>
  );
});
