import React, { memo } from 'react';
import { getLocale, setLocale } from 'umi';
import c from 'classnames';
import styles from './I18nSwitch.less';

interface Props {
  className?: string;
}

export default memo(function I18nSwitch({ className = '' }: Props) {
  const currentLocal = getLocale();

  return (
    <div className={c(styles.I18nSwitch, className)}>
      <span
        className={c(styles.i18nItem, {
          [styles.active]: currentLocal === 'en-US',
        })}
        onClick={() => setLocale('en-US')}
      >
        EN
      </span>
      /
      <span
        className={c(styles.i18nItem, {
          [styles.active]: currentLocal === 'zh-CN',
        })}
        onClick={() => setLocale('zh-CN')}
      >
        中文简体
      </span>
    </div>
  );
});
