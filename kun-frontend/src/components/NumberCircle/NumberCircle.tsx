import React, { memo } from 'react';
import styles from './NumberCircle.less';

interface Props {
  number: number;
}

export default memo(function NumberCircle({ number }: Props) {
  return <div className={styles.NumberCircle}>{number}</div>;
});
