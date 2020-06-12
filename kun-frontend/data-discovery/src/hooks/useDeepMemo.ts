import { useRef } from 'react';
import { equal } from '@wry/equality';

function useDeepMemo<TKey, TValue>(memoFn: () => TValue, key: TKey): TValue {
  const ref = useRef<{ key: TKey; value: TValue }>();

  if (!ref.current || !equal(key, ref.current.key)) {
    ref.current = { key, value: memoFn() };
  }

  return ref.current.value;
}

export default useDeepMemo;
