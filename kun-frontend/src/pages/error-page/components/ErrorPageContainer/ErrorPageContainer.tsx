import React, { memo } from 'react';
import logo from '@/assets/images/kun-logo.png';
import useI18n from '@/hooks/useI18n';
import miotechLogo from '@/assets/images/miotech_horizontal.png';

import styles from './ErrorPageContainer.less';

interface Props {
  children: React.ReactNode;
}
export default memo(function ErrorPageContainer({ children }: Props) {
  const t = useI18n();
  return (
    <div className={styles.ErrorPageContainer}>
      <div className={styles.titleRow}>
        <span className={styles.logoArea}>
          <img className={styles.logoImage} src={logo} alt="Logo" />
          <span className={styles.appName}>{t('common.app.name')}</span>
        </span>
      </div>

      <div className={styles.content}>{children}</div>

      <div className={styles.footer}>
        <div />
        <img className={styles.mioLogo} src={miotechLogo} alt="" />
        <span className={styles.footerContent}>
          ©2016-2020 MioTech Techonlogy. All rights reserved.
        </span>
      </div>
    </div>
  );
});
