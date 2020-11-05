import React, { memo } from 'react';
import c from 'clsx';
import { LeftOutlined } from '@ant-design/icons';

import styles from './SideCard.less';

interface Props {
  isExpanded: boolean;
  children: React.ReactNode;
  className?: string;
  width?: number;
  onClickExpandButton: (expanded: boolean) => void;
}

export default memo(function SideCard({
  isExpanded,
  children,
  className = '',
  width = 440,
  onClickExpandButton,
}: Props) {
  return (
    <div
      style={{ width }}
      className={c(styles.SideCard, className, {
        [styles.notExpanded]: !isExpanded,
      })}
    >
      <div
        className={styles.expandButton}
        onClick={() => {
          onClickExpandButton(!isExpanded);
        }}
      >
        <LeftOutlined
          className={c(styles.buttonIcon, {
            [styles.expandedIcon]: isExpanded,
          })}
        />
      </div>
      <div className={styles.content}>{children}</div>
    </div>
  );
});
