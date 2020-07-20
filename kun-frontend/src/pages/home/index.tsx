import React from 'react';
import { Redirect } from 'umi';
import usePermissions from '@/hooks/usePermissions';

export default function Home() {
  const dataDiscoveryPermission = usePermissions(['DATA_DISCOVERY']);
  const pdfPermission = usePermissions(['PDF_GENERAL', 'PDF_COA']);

  if (dataDiscoveryPermission) {
    return <Redirect to="/data-discovery" />;
  }

  if (pdfPermission) {
    return <Redirect to="/pdf" />;
  }

  return <div />;
}
