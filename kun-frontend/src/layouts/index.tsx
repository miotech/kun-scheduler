import React, { useEffect } from 'react';
import { IRouteComponentProps, IRoute } from 'umi';
import { Provider } from 'react-redux';
import { store } from '@/rematch/store';
import useI18n from '@/hooks/useI18n';

import DefaultLayout from './DefaultLayout/DefaultLayout';

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

  return (
    <Provider store={store}>
      <DefaultLayout route={route as IRoute}>{children}</DefaultLayout>
    </Provider>
  );
}
