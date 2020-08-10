import React, { memo } from 'react';
import logo from '@/assets/images/kun-logo.png';
import useI18n from '@/hooks/useI18n';
import chrome from './chrome.png';
import edge from './edge.png';
import miotechLogo from './miotech_horizontal.png';
import styles from './UnsupportedBrowserPage.less';

export default memo(function UnsupportedBrowserPage() {
  const t = useI18n();
  return (
    <div className={styles.UnsupportedBrowserPage}>
      <div className={styles.titleRow}>
        <span className={styles.logoArea}>
          <img className={styles.logoImage} src={logo} alt="Logo" />
          <span className={styles.appName}>{t('common.app.name')}</span>
        </span>
      </div>

      <div className={styles.content}>
        <div className={styles.contentTitle}>
          {t('common.unsupported.title')}
        </div>
        <div className={styles.contentMessage}>
          {t('common.unsupported.message')}
        </div>
        <div className={styles.browserRow}>
          <div className={styles.browserItem} style={{ marginRight: 54 }}>
            <img className={styles.logoImg} src={chrome} alt="" />
            <span className={styles.logoTitle}>Chrome</span>
          </div>

          <div className={styles.browserItem}>
            <img className={styles.logoImg} src={edge} alt="" />
            <span className={styles.logoTitle}>Edge</span>
          </div>
        </div>
      </div>

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
