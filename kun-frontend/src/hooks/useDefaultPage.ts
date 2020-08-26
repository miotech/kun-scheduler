import usePermissions from '@/hooks/usePermissions';

/**
 * 根据权限得到默认页面
 */
export default function useDefaultPage() {
  const dataDiscoveryPermission = usePermissions(['DATA_DISCOVERY']);
  const pdfGeneralPermission = usePermissions(['PDF_GENERAL']);
  const pdfCoaPermission = usePermissions(['PDF_COA']);

  if (dataDiscoveryPermission) {
    return '/data-discovery';
  }

  if (pdfGeneralPermission) {
    return '/pdf';
  }

  if (pdfCoaPermission) {
    return '/pdf-extract';
  }

  return null;
}
