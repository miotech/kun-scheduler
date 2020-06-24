/* eslint-disable react/no-array-index-key */
import React, { memo } from 'react';

import styles from './LineList.less';

interface Props {
  children: React.ReactNodeArray;
}

export default memo(function LineList({ children }: Props) {
  return (
    <div className={styles.lineList}>
      {React.Children.map(children, (child, i) => (
        <div key={i} className={styles.itemContainer}>
          {child}
        </div>
      ))}
    </div>
  );
});
