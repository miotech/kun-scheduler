import React from 'react';
import { Redirect } from 'umi';
import useDefaultPage from '@/hooks/useDefaultPage';

export default function RedirectDefaultPage() {
  const defaultPagePath = useDefaultPage();

  if (defaultPagePath) {
    return <Redirect to={defaultPagePath} />;
  }

  // TODO: 跳转到401页面
  return <div />;
}
