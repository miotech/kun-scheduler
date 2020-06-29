import { EffectCallback, useRef } from 'react';
import { useDebounceEffect } from 'ahooks';
import { DebounceOptions } from 'ahooks/es/useDebounce/debounceOptions';

/**
 * A debounced effect which will not trigger for the first time after mounted
 * @param effect
 * @param deps
 * @param options
 */
export default function useDebouncedUpdateEffect(
  effect: EffectCallback,
  deps?: ReadonlyArray<any>,
  options?: DebounceOptions
) {
  const isMounted = useRef<boolean>(false);

  useDebounceEffect(() => {
    if (!isMounted.current) {
      isMounted.current = true;
    } else {
      return effect();
    }
    return undefined;
  }, deps, options);
}
