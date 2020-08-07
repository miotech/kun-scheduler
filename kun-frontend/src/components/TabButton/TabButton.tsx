import React, { memo } from 'react';
import c from 'classnames';
import styles from './TabButton.less';

interface Props {
  title: string;
  isActive: boolean;
  value: string;
  onClick: (v: string) => void;
  className?: string;
}

export default memo(function TabButton({
  title,
  isActive,
  value,
  onClick,
  className = '',
}: Props) {
  return (
    <div
      className={c(styles.TabButton, className, { [styles.active]: isActive })}
      onClick={() => {
        if (!isActive) {
          onClick(value);
        }
      }}
    >
      {title}
    </div>
  );
});
