import React, { useEffect, useState } from 'react';

function useElementVisible(
  parentElRef: React.RefObject<HTMLDivElement>,
  elementRef: React.MutableRefObject<any>,
  hide: boolean | undefined = false,
) {
  const [visible, setVisible] = useState(false);
  useEffect(() => {
    const currentParentEl = parentElRef.current;
    const element = elementRef.current;
    const func = () => {
      if (!visible && !hide) {
        const viewPortHeight =
          currentParentEl?.offsetHeight ?? currentParentEl?.clientHeight ?? 0;
        const top =
          element.getBoundingClientRect() &&
          element.getBoundingClientRect().top;
        const bottom =
          element.getBoundingClientRect() &&
          element.getBoundingClientRect().bottom;
        if (top <= viewPortHeight + 500 && bottom >= -500) {
          setVisible(true);
        }
      }
    };
    func();
    if (currentParentEl) {
      currentParentEl.addEventListener('scroll', func);
    }
    return () => {
      if (currentParentEl) {
        currentParentEl.removeEventListener('scroll', func);
      }
    };
  }, [visible, setVisible, parentElRef, elementRef, hide]);
  return visible;
}

export default useElementVisible;
