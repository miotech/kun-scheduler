import React, { memo, useMemo } from 'react';
import { Layout } from 'antd';
import { IRoute } from 'umi';
import { KunSpin } from '@/components/KunSpin';
import { useSelector } from 'react-redux';
import { RootState } from '@/rematch/store';

import useI18n from '@/hooks/useI18n';

import Header from '@/components/Header/Header';
import Sider from '@/components/Sider/Sider';
import Breadcrumb from '@/components/Breadcrumb/Breadcrumb';

import css from './DefaultLayout.less';

const { Content } = Layout;

interface Props {
  children: React.ReactNode;
  route: IRoute;
  asBlock?: boolean;
}

const HOME_PATH = '/';

const useRouteMatch = (route: IRoute) => {
  const matchPath = useSelector((state: RootState) => state.route.currentMatchPath);

  return useMemo(() => {
    const findRoute = (traceRoute: IRoute, findPath: string): IRoute | undefined => {
      if (traceRoute.path === findPath) {
        return traceRoute;
      }

      const routes = traceRoute.routes ?? [];

      const findedRoute = routes.find(routeItem => {
        const { path } = routeItem;
        if (!path || path === HOME_PATH) return false;

        if (findPath.includes(path)) return true;

        return false;
      });

      if (!findedRoute) return undefined;

      return findRoute(findedRoute, findPath);
    };

    return findRoute(route, matchPath);
  }, [matchPath, route]);
};

export default memo(function DefaultLayout({ children, route, asBlock }: Props) {
  const isLoading = useSelector((state: RootState) => state.user.whoamiLoading);

  const t = useI18n();
  const matchRoute = useRouteMatch(route);

  return (
    <KunSpin
      asBlock={typeof asBlock === 'boolean' ? asBlock : false}
      wrapperClassName={css.spinContainer}
      spinning={isLoading}
      tip={t('common.loading')}
    >
      <Layout style={{ height: '100%' }}>
        <Header />
        <Layout className={css.siderAndContent}>
          <Sider route={route} />
          <Content className={css.content}>
            {!matchRoute?.hiddeSubHeader && (
              <div className="dafault-layout-subheader">
                <Breadcrumb route={route} />
              </div>
            )}
            <div className={css.contentInner}>{children}</div>
          </Content>
        </Layout>
      </Layout>
    </KunSpin>
  );
});
