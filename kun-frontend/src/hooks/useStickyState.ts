import React from 'react';

/**
 * Persisting a React state in localStorage so that we can keep it sync even if page refreshed
 * @param defaultValue the default value to be provided if key not persisted in local storage
 * @param key the key to persist that state in local storage
 */
export function useStickyState<T = any>(defaultValue: T, key: string): [T, React.Dispatch<React.SetStateAction<T>>] {
  const [value, setValue] = React.useState<T>(() => {
    const stickyValue = window.localStorage.getItem(key);
    return stickyValue !== null
      ? JSON.parse(stickyValue)
      : defaultValue;
  });

  React.useEffect(() => {
    window.localStorage.setItem(key, JSON.stringify(value));
  }, [key, value]);

  return [value, setValue];
}
