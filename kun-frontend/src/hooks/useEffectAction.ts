import { useState, useEffect } from 'react';

import useDeepMemo from './useDeepMemo';

function useEffectAction({
  action,
  effectBody = {},
  disabled = false,
}: {
  action: (...args: any[]) => Promise<any>;
  effectBody?: {
    [key: string]: any;
  };
  disabled?: boolean;
}) {
  const [isLoading, setIsLoading] = useState(true);

  const cachedBody = useDeepMemo(() => effectBody, [effectBody]);

  useEffect(() => {
    let ignore = false;

    const runAction = async () => {
      if (disabled) {
        setIsLoading(false);
        return;
      }

      setIsLoading(true);
      action().then(() => {
        if (!ignore) {
          setIsLoading(false);
        }
      });
    };

    runAction();

    return () => {
      ignore = true;
    };
  }, [action, cachedBody, disabled]);

  return isLoading;
}

export default useEffectAction;
