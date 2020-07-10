import { useHistory } from 'umi';
import { useCallback, useMemo } from 'react';

// 得到当前url中的backurl
export default function useBackUrlFunc() {
  const history = useHistory();

  // 此处 umi 的bug, type声明中没有 query
  const backUrl: string | null = useMemo(
    () => (history.location as any)?.query?.backUrl ?? null,
    [history.location],
  );

  const getBackUrl = useCallback(
    defaultUrl => {
      if (backUrl) {
        return backUrl;
      }
      return defaultUrl;
    },
    [backUrl],
  );

  return { getBackUrl, backUrl };
}
