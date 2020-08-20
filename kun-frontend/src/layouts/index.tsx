import React, { useEffect } from 'react';
import { IRouteComponentProps, IRoute, Route } from 'umi';
import { Provider } from 'react-redux';
import { QueryParamProvider } from 'use-query-params';
import { store } from '@/rematch/store';
import useI18n from '@/hooks/useI18n';
import BrowserCheck from '@/components/BrowserCheck/BrowserCheck';

import DefaultLayout from './DefaultLayout/DefaultLayout';
import NoBreadcrumbLayout from './NoBreadcrumbLayout/NoBreadcrumbLayout';

export default function Layout({
  children,
  location,
  route,
}: IRouteComponentProps) {
  const t = useI18n();

  useEffect(() => {
    window.t = t;
  }, [t]);

  if (location.pathname === '/login') {
    return <Provider store={store}>{children}</Provider>;
  }

  if (location.pathname.startsWith('/pdf')) {
    return (
      <BrowserCheck>
        <QueryParamProvider ReactRouterRoute={Route}>
          <Provider store={store}>
            <NoBreadcrumbLayout route={route as IRoute}>
              {children}
            </NoBreadcrumbLayout>
          </Provider>
        </QueryParamProvider>
      </BrowserCheck>
    );
  }

  return (
    <BrowserCheck>
      <QueryParamProvider ReactRouterRoute={Route}>
        <Provider store={store}>
          <DefaultLayout route={route as IRoute}>{children}</DefaultLayout>
        </Provider>
      </QueryParamProvider>
    </BrowserCheck>
  );
}
