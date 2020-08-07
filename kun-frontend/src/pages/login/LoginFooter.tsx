import React, { memo } from 'react';
import MiotechLogo from '@/assets/images/logo.svg';

import styles from './index.less';

export const LoginFooter: React.FC<{}> = memo(() => {
  return (
    <footer className={styles.Footer}>
      <span aria-label="miotech-logo">
        <img className={styles.MiotechLogo} src={MiotechLogo} alt="miotech" />
      </span>
      <span aria-label="copyright">
        ©2016-2020 MioTech Technology. All rights reserved.
      </span>
    </footer>
  );
});
