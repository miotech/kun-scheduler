import React, { memo } from 'react';
import { Layout, Spin } from 'antd';
import { IRoute } from 'umi';
import { useDispatch, useSelector } from 'react-redux';
import { RootDispatch, RootState } from '@/rematch/store';

import useI18n from '@/hooks/useI18n';

import Header from '@/components/Header/Header';
import Sider from '@/components/Sider/Sider';
import Breadcrumb from '@/components/Breadcrumb/Breadcrumb';

import css from './DefaultLayout.less';

const { Content } = Layout;

interface Props {
  children: React.ReactNode;
  route: IRoute;
}

export default memo(function DefaultLayout({ children, route }: Props) {
  const isLoading = useSelector((state: RootState) => state.user.whoamiLoading);

  const t = useI18n();

  return (
    <Spin spinning={isLoading} tip={t('common.loading')}>
      <Layout>
        <Header />
        <div className={css.subHeader}>
          <Breadcrumb route={route} />
        </div>
        <Layout className={css.siderAndContent}>
          <Sider route={route} />
          <Content className={css.content}>{children}</Content>
        </Layout>
      </Layout>
    </Spin>
  );
});
