import React, { memo } from 'react';
import c from 'classnames';
import styles from './TabBar.less';

interface Item {
  title: string;
  value: string;
}

interface Props {
  items: Item[];
  className?: string;
  currentValue: string;
  onChange: (v: string) => void;
}

export default memo(function TabBar({
  items,
  className = '',
  currentValue,
  onChange,
}: Props) {
  return (
    <div className={c(styles.tabBar, className)}>
      {items.map(item => (
        <div
          key={item.value}
          className={c(styles.item, {
            [styles.active]: currentValue === item.value,
          })}
          onClick={() => onChange(item.value)}
        >
          {item.title}
        </div>
      ))}
    </div>
  );
});
