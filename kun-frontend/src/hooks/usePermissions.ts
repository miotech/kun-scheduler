import useRedux from './useRedux';

/**
 * 是否包含其中之一的权限
 * @param currentPermissions
 */
export default function usePermissions(currentPermissions?: string[]) {
  const { selector } = useRedux(state => state.user);
  const { permissions } = selector;

  if (!currentPermissions) {
    return true;
  }
  let hasPermission = false;
  currentPermissions.forEach(i => {
    if (permissions.includes(i)) {
      hasPermission = true;
    }
  });

  return hasPermission;
}
