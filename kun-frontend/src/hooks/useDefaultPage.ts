import usePermissions from '@/hooks/usePermissions';

/**
 * 根据权限得到默认页面
 */
export default function useDefaultPage() {
  const dataDiscoveryPermission = usePermissions(['DATA_DISCOVERY']);

  if (dataDiscoveryPermission) {
    return '/data-discovery';
  }

  return null;
}
