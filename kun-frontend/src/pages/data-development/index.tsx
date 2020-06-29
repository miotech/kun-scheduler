import React from 'react';
import styles from './index.less';

const DataDevelopmentPage: React.FC<any> = props => {
  const { ...restProps } = props;

  return (
    <main className={styles.Page} {...restProps}>
      Data development page
    </main>
  );
};

export default DataDevelopmentPage;
