import { useState, useCallback, useMemo } from 'react';
import { history } from 'umi';
import qs from 'qs';

type StateType = string | string[];

export default function useUrlQueryState<T extends StateType>(
  field: string,
  defaultValue: T,
): [T, (newState: T) => void] {
  const { pathname, search } = history.location;
  const query = useMemo(() => qs.parse(search.replace('?', '')), [search]);

  let initValue: T;
  if (query[field]) {
    initValue = query[field] as T;
  } else {
    initValue = defaultValue;
  }
  const [state, setState] = useState<T>(initValue);

  const setQueryState = useCallback(
    (newState: T) => {
      setState(newState);
      const newQuery = { ...query, [field]: newState };
      const newPath = `${pathname}?${qs.stringify(newQuery)}`;
      history.replace(newPath);
    },
    [field, pathname, query],
  );

  return [state, setQueryState];
}
