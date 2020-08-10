import React, { lazy, Suspense, useMemo } from 'react';

import { Spin } from 'antd';

import {
  isBrowserSupported,
  supportedBrowsers,
} from '@/utils/browserDetection';

const BrowserNotSupportedPage = lazy(() =>
  import('../UnsupportedBrowserPage/UnsupportedBrowserPage'),
);

function BrowserCheck({ children }: { children: React.ReactNode }) {
  const isSupported = useMemo(
    () => supportedBrowsers.map(isBrowserSupported).some(Boolean),
    [],
  );

  return (
    <Suspense fallback={<Spin spinning />}>
      {isSupported ? children : <BrowserNotSupportedPage />}
    </Suspense>
  );
}

export default BrowserCheck;
