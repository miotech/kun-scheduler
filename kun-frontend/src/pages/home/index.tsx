import React from 'react';
import { Redirect } from 'umi';
import usePermissions from '@/hooks/usePermissions';

export default function Home() {
  const dataDiscoveryPermission = usePermissions(['DATA_DISCOVERY']);
  const pdfGeneralPermission = usePermissions(['PDF_GENERAL']);
  const pdfCoaPermission = usePermissions(['PDF_COA']);

  if (dataDiscoveryPermission) {
    return <Redirect to="/data-discovery" />;
  }

  if (pdfGeneralPermission) {
    return <Redirect to="/pdf" />;
  }

  if (pdfCoaPermission) {
    return <Redirect to="/pdf-extract" />;
  }

  return <div />;
}
