import React, { useLayoutEffect } from 'react';
import { IRouteComponentProps, IRoute, Route } from 'umi';
import { Spin } from 'antd';
import { Provider } from 'react-redux';
import { QueryParamProvider } from 'use-query-params';
import { KunSpinIndicator } from '@/components/KunSpin/KunSpinIndicator';
import { store } from '@/rematch/store';
import useI18n from '@/hooks/useI18n';
import BrowserCheck from '@/components/BrowserCheck/BrowserCheck';

import DefaultLayout from './DefaultLayout/DefaultLayout';

function initializeKunSpinIndicator() {
  // @ts-ignore
  // eslint-disable-next-line no-underscore-dangle
  if (!window.__KUN_SPIN_INDICATOR_INITIALIZED) {
    Spin.setDefaultIndicator(KunSpinIndicator);
    // @ts-ignore
    // eslint-disable-next-line no-underscore-dangle
    window.__KUN_SPIN_INDICATOR_INITIALIZED = true;
  }
}

export default function Layout({ children, location, route }: IRouteComponentProps) {
  const t = useI18n();

  initializeKunSpinIndicator();

  useLayoutEffect(() => {
    window.t = t;
    document.documentElement.setAttribute('lang', t('common.lang'));
  }, [t]);

  if (location.pathname === '/login') {
    return <Provider store={store}>{children}</Provider>;
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
