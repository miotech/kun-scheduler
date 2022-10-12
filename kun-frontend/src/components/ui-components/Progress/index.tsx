import React, { ReactNode, useRef, useState } from 'react';
import { Progress as AntProgress, ProgressProps } from 'antd';
import { useMount } from 'ahooks';

import styles from './index.less';

interface Props extends ProgressProps {
  label?: string | ReactNode;
}

const Progress: React.FC<Props> = ({ label, format, percent, showInfo, ...resetProps }) => {
  const [infoOffset, setInfoOffset] = useState(0);
  const containerRef = useRef<HTMLDivElement>(null);

  useMount(() => {
    const containerEle = containerRef.current;
    if (containerEle) {
      const progressBarEle = containerEle.querySelector('.ant-progress-bg');
      const progressInnerEle = containerEle.querySelector('.ant-progress-inner');
      const offset = (progressInnerEle?.clientWidth || 0) - (progressBarEle?.clientWidth || 0);
      setInfoOffset(offset);
    }
  });

  return (
    <div className={styles.Container} ref={containerRef}>
      {label && <div className={styles.Label}>{label}</div>}
      <AntProgress percent={percent} {...resetProps} showInfo={false} trailColor="transparent" />
      {showInfo && (
        <div style={{ transform: `translateX(-${infoOffset}px)` }} className={styles.Info}>
          {format ? format(percent) : percent}
        </div>
      )}
    </div>
  );
};

export { Progress };
