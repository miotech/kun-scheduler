/**
 * 根据权限得到默认页面
 */
export default function useDefaultPage() {
  // 如果后续需要鉴权逻辑处理，则使用 usePermissions hook
  return '/monitoring-dashboard';
}
