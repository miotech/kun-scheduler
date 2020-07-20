import useRedux from './useRedux';

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
