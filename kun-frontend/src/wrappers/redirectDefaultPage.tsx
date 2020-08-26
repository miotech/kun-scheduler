import React from 'react';
import { Redirect } from 'umi';
import useDefaultPage from '@/hooks/useDefaultPage';

export default function RedirectDefaultPage() {
  const defaultPagePath = useDefaultPage();

  if (defaultPagePath) {
    return <Redirect to={defaultPagePath} />;
  }

  return <div />;
}
